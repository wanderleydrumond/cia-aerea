package mappers;

import javax.ejb.Stateless;

import dtos.UserDTO;
import entities.User;

/**
 * Class responsible by transform <code>User</code> data that transits between backend and frontend.
 * 
 * @author Wanderley Drumond
 *
 */
@Stateless
public class UserMapper {
	/**
	 * Changes a <code>User</code> DTO object into a <code>User</code> Entity object.
	 * 
	 * @param userDTO the object that will be transformed into Entity object
	 * @return the Entity resultant object
	 */
	public User toEntity(UserDTO userDTO) {
		User user = new User();
		
		user.setName(userDTO.getName());
		user.setUsername(userDTO.getUsername());
		user.setPassword(userDTO.getPassword());
		if (userDTO.getToken() != null) {
			user.setToken(userDTO.getToken());
		}
		user.setRole(userDTO.getRole());
		
		return user;
	}
	
	/**
	 * Changes a <code>User</code> Entity object into a <code>User</code> DTO object.
	 * 
	 * @param user the object that will be transformed into DTO object
	 * @return the DTO resultant object
	 */
	public UserDTO toDTO(User user) {
		UserDTO userDTO = new UserDTO();
		
		userDTO.setName(user.getName());
		userDTO.setUsername(user.getUsername());
		userDTO.setPassword(user.getPassword());
		if (user.getToken() != null) {
			userDTO.setToken(user.getToken());
		}
		userDTO.setRole(user.getRole());
		
		return userDTO;
	}
}