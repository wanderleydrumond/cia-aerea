package services;

import java.io.Serializable;
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
			System.err.println("Catch Exception countOccupiedSeatsByFlightId() in TicketService");
			System.err.println(exception.getClass().getName());
			exception.printStackTrace();
			
			return null;
		}
	}

}