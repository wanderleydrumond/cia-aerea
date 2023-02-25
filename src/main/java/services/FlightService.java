package services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import daos.FlightDAO;
import daos.TicketDAO;
import dtos.FlightDTO;
import entities.Flight;
import mappers.FlightMapper;

/**
 * Class that contains all the programmatic logic regarding the flight.
 * 
 * @author Wanderley Drumond
 *
 */
@RequestScoped
public class FlightService implements Serializable {

	/**
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Object that contains all methods to manipulates database regarding flights table.
	 */
	@Inject
	private FlightDAO flightDAO;
	
	/**
	 * Object that contains methods from <code>Flight</code> object to switch it between Entity and DTO formats.
	 */
	@Inject
	private FlightMapper flightMapper;
	
	@Inject
	private TicketDAO ticketDAO;

	/**
	 * Creates a new flight for the logged user.
	 * 
	 * @param flightDTO flight data to be inserted
	 * @return the new flight DTO created
	 */
	public FlightDTO create(FlightDTO flightDTO) {
		Flight flight = flightMapper.toEntity(flightDTO);
		String destination = flight.getDestination();
		String code = generateCode(destination);
		
		flight.setCode(code);
		
		flightDAO.persist(flight);
		
		flightDTO.setId(flight.getId());
		flightDTO.setCode(flight.getCode());
		
		return flightDTO;
	}

	/**
	 * Generates a flight code: 3 initial letters from destination in upper case + "_" + last flight id;
	 * 
	 * @param destination name which will be cropped the initials  
	 * @return the generated flight code
	 */
	private String generateCode(String destination) {
		String initials = getThreeFirstLetters(destination);
		Integer lastId = flightDAO.findNewestId();
		
		return initials + "_" + (lastId + 1);
	}
	
	/**
	 * <p>Gets the first three letters from each word containing in the given sentence/word.</p>
	 * <p><code>create()</code> auxiliary method.</p>
	 * 
	 * <p>
     * 	<strong>Examples:</strong>
     * 	<blockquote>
     * 		 "United States of America" returns "USA"<br>
     * 		 "Dominican Republic" returns "DOR"<br>
     * 		 "Brazil", returns "BRA"
     * 	</blockquote>
     * </p>
	 * 
	 * @param destination word or sentence to be split.
	 * @see <a href = "https://stackoverflow.com/questions/40090776/what-does-s-split-s-means-here-in-the-below-code">What does s.split("\\s+")) means here in the below code? [duplicate]</a>
	 * @see <a href = https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.>Class Pattern</a>
	 * @return the resultant String, in upper case
	 * 		  <ul> If destination have:
	 * 			<li>At least 3 words: the first letter of the first 3 words bigger than 3 letters</li>
	 * 			<li>Two words: the first two letters of the first word and the first letter of the second one</li>
	 * 			<li>One single word: the first three letters</li>
	 * 		  </ul>
	 */
	private String getThreeFirstLetters(String destination) {
	    String[] words = destination.split("\\s+");
	    StringBuilder firstLetters = new StringBuilder();
	    // Se o nome do lugar tiver no mínimo 3 palavras
	    if (words.length >= 3) {
			for (String word : words) {
				if (word.length() >= 3) {
					firstLetters.append(word.toUpperCase().charAt(0));
				}
			}
		} else if (words.length == 2) { // Se o nome do lugar tiver somente duas palavras
			for (int index = 0; index < 2; index++) {
				if (index == 0) { // Pega as duas primeiras letras da palavra em maiúsculas
					String twoFirstLetters = words[index].substring(0, 2);
					firstLetters.append(twoFirstLetters.toUpperCase());
				} else { // Pega a primeira letra da palavra em maiúscula
					String singleLetter = words[index].substring(0, 1);
					firstLetters.append(singleLetter.toUpperCase());
				}
			}
		} else { // Se o nome do lugar tiver somente uma palavra
			firstLetters.append(words[0].toUpperCase().substring(0, 3));
		}
	    
		return firstLetters.toString();
	}

	/**
	 * Gets all flights which have available seats.
	 * 
	 * @return
	 * 		  <ul>if requisition was:
	 * 			<li>successful: the list of flights with available seats</li>
	 * 			<li>unsuccessful: null</li>
	 * 		  </ul>
	 */
	public List<FlightDTO> getAllAvailables() {
		try {
			List<Flight> flightsFound = flightDAO.findAll();
			List<FlightDTO> flightsToDisplay = new ArrayList<FlightDTO>();
			
			for (Flight flightElement : flightsFound) {
				
				Long totalTicketsByFlight = ticketDAO.countOccupiedSeatsByFlightId(flightElement.getId());
				Long availableSeats = flightElement.getTotalSeats() - totalTicketsByFlight;
				
				if (availableSeats > 0) {
					FlightDTO flightDTO = flightMapper.toDTO(flightElement);
					flightDTO.setFreeSeats(availableSeats.intValue());
					flightsToDisplay.add(flightDTO);
				}
			}
			
			return flightsToDisplay;
		} catch (Exception exception) {
			System.err.println("Catch getAllAvailables() in FlightService");
			System.err.println(exception.getClass().getName());
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Gets the flight object by the given id.
	 * 
	 * @param idFlight flight object primary key.
	 * @return
	 * 		  <ul> An Optional object which has inside:
	 * 			<li>a flight object if the correspondent flight was found</li>
	 * 			<li>empty if the correspondent flight was not found</li>
	 * 		  </ul>
	 */
	public Optional<Flight> getById(Integer idFlight) {
		try {
			Optional<Flight> optionalFlightFound = flightDAO.find(idFlight);
			
			return optionalFlightFound;
		} catch (Exception exception) {
			exception.printStackTrace();
			
			return Optional.empty();
		}
	}
}