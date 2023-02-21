package services;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import daos.FlightDAO;
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
	 * Object that contains all user service methods.
	 */
	@Inject
	UserService userService;
	
	/**
	 * Object that contains all methods to manipulates database regarding flights table.
	 */
	@Inject
	FlightDAO flightDAO;
	
	/**
	 * Object that contains methods from <code>Flight</code> object to switch it between Entity and DTO formats.
	 */
	@Inject
	FlightMapper flightMapper;

	/**
	 * Creates a new flight for the logged user.
	 * 
	 * @param flightDTO flight data to be inserted
	 * @return the new flight created
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
	 * 			<li>Two words: the first two letters os the frist word and the first letter of the second one</li>
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
}