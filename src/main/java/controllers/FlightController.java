package controllers;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.FlightDTO;
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
	FlightService flightService;
	
	@Inject
	UserService userService;
	
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
}