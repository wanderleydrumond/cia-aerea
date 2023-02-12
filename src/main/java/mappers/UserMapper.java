package mappers;

import javax.ejb.Stateless;

import dtos.UserDTO;
import entities.User;

@Stateless
public class UserMapper {
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