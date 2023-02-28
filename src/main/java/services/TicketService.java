package services;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import daos.TicketDAO;
import dtos.TicketDTO;
import entities.Flight;
import entities.Ticket;
import entities.User;
import enums.Role;

/**
 * Class that contains all the programmatic logic regarding the ticket.
 * 
 * @author Wanderley Drumond
 *
 */
@RequestScoped
public class TicketService implements Serializable {

	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialisation to verify that the sender and receiver of a serialised object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;
	
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
	 * Object that contains all methods to manipulates database regarding tickets table.
	 */
	@Inject
	private TicketDAO ticketDAO;

	/**
	 * Creates a new ticket.
	 * 
	 * @param token		the authorisation key of the logged user
	 * @param ticketDTO the information of the new ticket to be created
	 * @return
	 * 		  <ul>
	 * 			<li>null, if user who will owns the ticket/take the fight not found in database</li>
	 * 			<li>a new ticketDTO object with it's id equal to: 
	 * 				<ul>
	 * 					<li>-1: if user who will buy the ticket not found in database</li>
	 * 					<li>-2: if CLIENT tries to buy new a ticket for another user</li>
	 * 					<li>-3: if user tries to buy a new ticket for a flight with no available seats</li>
	 * 				</ul>
	 * 			</li>
	 * 			<li>the ticketDTO object, updated with its id and user id and flight id</li>
	 * 		  </ul>
	 */
	public TicketDTO create(String token, TicketDTO ticketDTO) {
		Ticket ticket = new Ticket();
		Optional<Flight> flight = flightService.getById(ticketDTO.getIdFlight());
		Optional<User> passenger = userService.getById(ticketDTO.getIdUser());
		Optional<User> buyer = userService.getByToken(token);
		
		// Se o utilizador que fará a viagem não for encontrado na base de dados
		if (passenger.isEmpty()) {
			return null;
		}
		
		// Se o utilizador que cria/compra a passagem não for encontrado (impossible) (401)
		if (buyer.isEmpty()) {
			TicketDTO expendableTicketDTO = new TicketDTO();
			
			expendableTicketDTO.setId(-1);
			
			return expendableTicketDTO;
		}
		
		// Se o utilizador que compra a passagem for um cliente e tentar comprá-la para outro utilizador (403)
		if (buyer.get().getRole().equals(Role.CLIENT) && passenger.get().getId() != buyer.get().getId()) {
			TicketDTO expendableTicketDTO = new TicketDTO();
			
			expendableTicketDTO.setId(-2);
			
			return expendableTicketDTO;
		}
		
		Long occupiedSeats = getOccupiedSeatsByFlightId(flight.get().getId());
		
		if (flight.get().getTotalSeats() <= occupiedSeats) {
			TicketDTO expendableTicketDTO = new TicketDTO();
			
			expendableTicketDTO.setId(-3);
			
			return expendableTicketDTO;
		}
		
		ticket.setFlightDetails(flight.get());
		ticket.setPassenger(passenger.get());
		ticket.setIsCanceled(false);
		ticket.setIsDeleted(false);
		
		ticketDAO.persist(ticket);
		
		ticketDTO.setId(ticket.getId());
		ticketDTO.setIdFlight(flight.get().getId());
		ticketDTO.setIdUser(passenger.get().getId());
		
		return ticketDTO;
	}
	
	/**
	 * Gets the amount of occupied seats by flight according to the given id.
	 * 
	 * @param idFlight primary key of the flight to be checked
	 * @return
	 * 		  <ul> If the request was:
	 * 			<li>Well succeeded: the amount of the occupied seats</li>
	 * 			<li>Bad succeeded: null</li>
	 * 		  </ul>
	 */
	public Long getOccupiedSeatsByFlightId(Integer idFlight) {
		try {
			return ticketDAO.countOccupiedSeatsByFlightId(idFlight);
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " countOccupiedSeatsByFlightId() in TicketService");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Gets the list of tickets for the given user id.
	 * 
	 * @param userId primary key of the user that owns the ticket
	 * @return
	 * 		  <ul> If the request was:
	 * 			<li>Well succeeded: the list of tickets DTO</li>
	 * 			<li>Bad succeeded: null</li>
	 * 		  </ul>
	 */
	public List<TicketDTO> getByUserId(int userId) {
		try {
			List<Ticket> ticketsFound = ticketDAO.findTicketsByUserId(userId);
			TicketDTO ticketDTO = new TicketDTO();
			List<TicketDTO> ticketsDTO = new ArrayList<>();
			
			ticketsFound.forEach(ticketElement -> {
				ticketDTO.setId(ticketElement.getId());
				ticketDTO.setIdFlight(ticketElement.getFlightDetails().getId());
				ticketDTO.setIdUser(ticketElement.getPassenger().getId());
				ticketDTO.setFlightCode(ticketElement.getFlightDetails().getCode());
				ticketDTO.setUserName(ticketElement.getPassenger().getName());
				ticketDTO.setFlightDestination(ticketElement.getFlightDetails().getDestination());
				ticketDTO.setFlightDepartTime(ticketElement.getFlightDetails().getDepartTime().toString());
				
				ticketsDTO.add(ticketDTO);
			});
			
			
			return ticketsDTO;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " getByUserId() in TicketService");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Sets the attribute isCanceled in <code>Ticket</code> object in database to true.
	 * 
	 * @param token		the authorisation key of the logged user
	 * @param ticketId	the primary key of the ticket
	 * @return
	 * 		  <ul>
	 * 			<li>A new <code>TicketDTO</code> with id:</li>
	 * 			<ul>
	 * 				<li><strong>-1</strong> if ticket was not found in database</li>
	 * 				<li><strong>-2</strong> if ticket is already cancelled</li>
	 * 				<li><strong>-3</strong> if date of action is not earlier that 1 day</li>
	 * 				<li><strong>-4</strong> if a client tries to cancel another user ticket</li>
	 * 			</ul>
	 * 				<li>A <code>TicketDTO</code> updated if everything goes well</li>
	 * 		  </ul>
	 */
	public TicketDTO cancelById(String token, int ticketId) {
		Optional<Ticket> optionalTicket = ticketDAO.find(ticketId);
		Flight flight = optionalTicket.get().getFlightDetails();
		User passenger = optionalTicket.get().getPassenger();
		Optional<User> loggedUser = userService.getByToken(token);
		TicketDTO ticketDTO = new TicketDTO();
		LocalDateTime oneDayEarlierDepart = flight.getDepartTime().toLocalDateTime().minusDays(1);
		
		// Se não encontrar o ticket com o id fornecido
		if (optionalTicket.isEmpty()) {
			ticketDTO.setId(-1);
			return ticketDTO;
		}
		
		// Se o ticket já estiver cancelado
		if (optionalTicket.get().getIsCanceled().equals(true)) {
			ticketDTO.setId(-2);
			return ticketDTO;
		}
		
		// Se a data do ticket for menor que 1 dia
		if (!LocalDateTime.now().isBefore(oneDayEarlierDepart)) {
			ticketDTO.setId(-3);
			return ticketDTO;
		}
		
		// Se o user for client e tentar cancelar de outro user
		if (loggedUser.get().getRole().equals(Role.CLIENT) && loggedUser.get().getId() != passenger.getId()) {
			ticketDTO.setId(-4);
			return ticketDTO;
		}
		
		optionalTicket.get().setIsCanceled(true);
		ticketDAO.merge(optionalTicket.get());
		
		ticketDTO.setId(optionalTicket.get().getId());
		ticketDTO.setIdFlight(flight.getId());
		ticketDTO.setFlightCode(flight.getCode());
		ticketDTO.setFlightDestination(flight.getDestination());
		ticketDTO.setFlightDepartTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(flight.getDepartTime()));
		ticketDTO.setIdUser(passenger.getId());
		ticketDTO.setUserName(passenger.getName());
		
		return ticketDTO;
	}

	/**
	 * Gets the amount of active tickets that have not yet taken place for the given user id.
	 * 
	 * @param userId primary key of the user to have their tickets count
	 * @return
	 * 		  <ul>
	 * 			<li>the amount tickets, if the requisition was successfully</li>
	 * 			<li>null, if any errors occurred</li>
	 * 		  </ul>
	 */
	public Integer getAllNonDeletedWithFutureFlightByUserId(Integer userId) {
		try {
			return ticketDAO.countAllNonDeletedWithFutureFlightByUserId(userId);
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " getAllNonDeletedWithFutureFlightByUserId() in TicketService");
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Soft delete all tickets of the given user id.
	 * 
	 * @param userId primary key of the user that owns the tickets to be soft deleted
	 * @return the amount of rows updated in database
	 */
	public Integer softDeleteByUserId(Integer userId) {
		List<Ticket> ticketsToDelete = ticketDAO.findTicketsByUserId(userId);
		ticketsToDelete.forEach(ticketElement -> {
			ticketElement.setIsDeleted(true);
			ticketDAO.merge(ticketElement);
		});
		
		return ticketsToDelete.size();
	}
}