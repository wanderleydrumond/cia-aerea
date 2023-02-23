package controllers;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.TicketDTO;
import services.TicketService;

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
			return Response.status(403).entity(message).build();
		}
		
		return Response.status(201).entity(newTicketDTO).build();
	}
}