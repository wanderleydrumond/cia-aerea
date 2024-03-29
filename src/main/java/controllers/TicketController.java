package controllers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.TicketDTO;
import entities.User;
import enums.Role;
import services.TicketService;
import services.UserService;

/**
 * Class that contains all requisition methods that refers to ticket.
 * 
 * @author Wanderley Drumond
 *
 */
@Path("/ticket")
public class TicketController {
	/**
	 * Object that contains all ticket service methods.
	 */
	@Inject
	private TicketService ticketService;
	
	/**
	 * Object that contains all user service methods.
	 */
	@Inject
	UserService userService;
	
	/**
	 * <p>Creates a new ticket.</p>
	 * <p>Called when a user buys a new ticket.</p>
	 * <ul>
	 * 	<li>CLIENT: </li> allowed to buy a new ticket only to himself.
	 * 	<li>EMPLOYEE and ADMINISTRATOR: </li> allowed to buy a new ticket for himself and any other user.
	 * </ul>
	 * 
	 * @param token		  the authorisation key of the logged user
	 * @param requestBody the information of the new ticket to be created
	 * @return
	 * 		  <ul>
	 * 			<li><strong>401 (UNAUTHORIZED)</strong>
	 * 				if:
	 * 				<ul>
	 * 					<li>token is null</li>
	 * 					<li>token is empty</li>
	 * 					<li>user who will buy the ticket not found in database</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li><strong>403 (FORBIDDEN)</strong> if CLIENT tries to buy new a ticket for another user
	 * 			</li>
	 * 			<li><strong>400 (BAD REQUEST)</strong> if:
	 * 				<ul>
	 * 					<li>user who will owns the ticket/take the fight not found in database</li>
	 * 					<li>user tries to buy a new ticket for a flight with no available seats</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li><strong>201 (CREATED)</strong> if new ticket was successfully created</li>
	 * 		  </ul>
	 */
	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@HeaderParam("token") String token, TicketDTO requestBody) {
		if (token == null || token.isBlank()) {
			String message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		TicketDTO newTicketDTO = ticketService.create(token, requestBody);
		
		if (newTicketDTO == null) {
			String message = "Passenger not found";
			return Response.status(400).entity(message).build();
		}
		
		if (newTicketDTO.getId() == -1) {
			String message = "Buyer not found";
			return Response.status(401).entity(message).build();
		}
		
		if (newTicketDTO.getId() == -2) {
			String message = "Client cannot buy ticket for other users";
			return Response.status(403).entity(message).build();
		}
		
		if (newTicketDTO.getId() == -3) {
			String message = "There are no available seats from this flight";
			return Response.status(400).entity(message).build();
		}
		
		return Response.status(201).entity(newTicketDTO).build();
	}
	
	/**
	 * Gets the ticket information by user's id.
	 * 
	 * @param token the authorisation key of the logged user
	 * @param id	the primary key of the user who owns the ticket
	 * @return
	 * 		  <ul>
	 * 			<li>
	 * 				<strong>401 (Unauthorised)</strong>
	 * 				if:
	 * 				<ul>
	 * 					<li>token is null</li>
	 * 					<li>token is empty</li>
	 * 					<li>user who will buy the ticket not found in database</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li><strong>403 (Forbidden)</strong> if logged user role is client and <code>userId</code> does not belong to him</li>
	 * 			<li><strong>200 (OK)</strong> if requisition was successfully done. Along with data requested.</li>
	 * 		  </ul>
	 */
	@Path("/by-user/{userId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByUserId(@HeaderParam("token") String token, @PathParam("userId") String id) {
		String message;
		
		if (token == null || token.isBlank()) {
			message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		Optional<User> loggedUser = userService.getByToken(token);
		
		if (loggedUser.isEmpty()) {
			message = "User not found in database";
			return Response.status(401).entity(message).build();
		}
		
		if (loggedUser.isPresent() && loggedUser.get().getRole().equals(Role.CLIENT)) {
			message = "Client cannot see tickets from another user";
			return Response.status(403).entity(message).build();
		}
		
		List<TicketDTO> ticketDTO = ticketService.getByUserId(Integer.parseInt(id));
		
		return Response.ok(ticketDTO).build();
	}
	
	/**
	 * Sets the attribute isCanceled in <code>Ticket</code> object in database to true.
	 * 
	 * @param token the authorisation key of the logged user
	 * @param id	the primary key of the ticket
	 * @return
	 * 		  <ul>
	 * 			<li>
	 * 				<strong>401 (Unauthorised)</strong>
	 * 				if:
	 * 				<ul>
	 * 					<li>token is null</li>
	 * 					<li>token is empty</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li><strong>404 (Not Found)</strong> if the informed ticked id does not exists in database</li>
	 * 			<li>
	 * 				<strong>400 (Bad Request)</strong>
	 * 				if:
	 * 				<ul>
	 * 					<li>ticket is already cancelled</li>
	 * 					<li>date of action is not earlier that 1 day</li>
	 * 					<li>a client tries to cancel another user ticket</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li><strong>200 (OK)</strong> the requisition was successfully done</li>
	 * 		  </ul>
	 */
	@Path("/cancel-by/{ticketId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancelById(@HeaderParam("token") String token, @PathParam("ticketId") String id) {
		String message;
		
		if (token == null || token.isBlank()) {
			message = "User not logged";
			return Response.status(401).entity(message).build();
		}
		
		TicketDTO ticketDTO = ticketService.cancelById(token, Integer.parseInt(id));
		
		switch (ticketDTO.getId()) {
		case -1:
			message = "Ticket not found";
			return Response.status(404).entity(message).build();

		case -2:
			message = "Ticket already canceled";
			return Response.status(400).entity(message).build();
			
		case -3:
			message = "Is not possible to cancel this ticket. Date expired.";
			return Response.status(400).entity(message).build();
			
		case -4:
			message = "Client cannot cancel ticket that belongs to another user";
			return Response.status(400).entity(message).build();
		}
		
		return Response.ok(ticketDTO).build();
	}
}