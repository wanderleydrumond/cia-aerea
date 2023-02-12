package mappers;

import java.sql.Timestamp;

import javax.ejb.Stateless;

import dtos.FlightDTO;
import entities.Flight;

@Stateless
public class FlightMapper {
	public Flight toEntity(FlightDTO flightDTO) {
		Flight flight = new Flight();
		
		flight.setCode(flightDTO.getCode());
		flight.setDestination(flightDTO.getDestination());
		flight.setTotalSeats(flightDTO.getTotalSeats());
		flight.setDepartTime(Timestamp.valueOf(flightDTO.getDepartTime()));
		
		return flight;
	}
	
	public FlightDTO toDTO(Flight flight) {
		FlightDTO flightDTO = new FlightDTO();
		
		flightDTO.setCode(flight.getCode());
		flightDTO.setDestination(flight.getDestination());
		flightDTO.setTotalSeats(flight.getTotalSeats());
		flightDTO.setDepartTime(flight.getDepartTime().toString());
		
		return flightDTO;
	}
}