package controllers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.FlightDTO;
import entities.User;
import enums.Role;
import services.FlightService;
import services.UserService;

/**
 * Class that contains all requisition methods that refers to flight.
 * 
 * @author Wanderley Drumond
 *
 */
@Path("/flight")
public class FlightController {
	/**
	 * Object that contains all flight service methods.
	 */
	@Inject
	private FlightService flightService;
	
	/**
	 * Object that contains all user service methods.
	 */
	@Inject
	private UserService userService;
	
	/**
	 * Creates a new flight.
	 * 
	 * @param token		the authorisation key of the logged user
	 * @param flightDTO the information of the new flight to be created
	 * @return
	 * 		  <ul>
	 * 			<li><strong>401 (Unauthorized)</strong> if the user does not have a token. (It's not logged)</li>
	 * 			<li><strong>403 (Forbidden)</strong></li>
	 * 				<ul>If: 
	 * 					<li>User that owns the given token does not exists in database (impossible)</li>
	 * 					<li>User role is CLIENT</li>
	 * 				</ul>
	 * 			<li><strong>201 (Created)</strong> if the flight was created successfully</li>
	 * 		  </ul>
	 */
	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@HeaderParam("token") String token, FlightDTO flightDTO) {
		if (token == null || token.isBlank()) {
			String message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		Role loggedUserRole = userService.getRoleLoggedUser(token);
		if (loggedUserRole == null || loggedUserRole.equals(Role.CLIENT)) {
			String message = "User not found or user role == CLIENT";
			return Response.status(403).entity(message).build();
		}
		
		FlightDTO newFlight = flightService.create(flightDTO);
		return Response.status(201).entity(newFlight).build();
	}
	
	/**
	 * Gets all flights with available seats.
	 * 
	 * @param token the authorisation key of the logged user
	 * @return
	 * 		  <ul>
	 * 			<li><strong>401 (Unauthorised)</strong> if the user does not have a token. (It's not logged)</li>
	 * 			<li><strong>200 (OK)</strong> if requisition was successfully answered</li>
	 * 		  </ul>
	 */
	@Path("/availables")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAvailables(@HeaderParam("token") String token) {
		if (token == null || token.isBlank()) {
			String message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		List<FlightDTO> flightsFound = flightService.getAllAvailables();
		
		return Response.ok(flightsFound).build();
	}
	
	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAll(@HeaderParam("token") String token) {
		if (token == null || token.isBlank()) {
			String message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		Optional<User> loggedUser = userService.getByToken(token);
		String message;
		
		if (loggedUser.isEmpty()) {
			message = "User not found in database";
			return Response.status(404).entity(message).build();
		}
		
		if (loggedUser.isPresent() && loggedUser.get().getRole().equals(Role.CLIENT)) {
			message = "Client cannot see flights which don't have available seats";
			return Response.status(403).entity(message).build();
		}
		
		List<FlightDTO> flightsDTO = flightService.getAll();
		return Response.ok(flightsDTO).build();
	}
}