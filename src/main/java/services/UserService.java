package services;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import daos.UserDAO;
import dtos.UserDTO;
import entities.User;
import enums.Role;
import mappers.UserMapper;

/**
 * Class that contains all the programmatic logic regarding the user.
 * 
 * @author Wanderley Drumond
 *
 */
@RequestScoped
public class UserService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Object that contains methods from <code>User</code> object to switch it between Entity and DTO formats.
	 */
	@Inject
	UserMapper userMapper;
	
	/**
	 * Object that contains all methods to manipulates database regarding users table.
	 */
	@Inject
	UserDAO userDAO;

	/**
	 * Registers a new user into the system.
	 *
	 * @param userDTO an object that contains the information for the new user
	 * @return true if the user was registered successfully, otherwise false
	 * 		<ul>
	 *			<li><code>TRUE</code> if the user was registered successfully</li>
	 *			<li><code>NULL</code> if error occurred, preventing the user from being saved</li> 	
	 * 		</ul>
	 */
	public Boolean signUp(UserDTO userDTO) {
		try {
			User user = userMapper.toEntity(userDTO);
			user.setRole(Role.CLIENT);
			
			userDAO.persist(user);
			
			return true;
		} catch (Exception exception) {
			exception.printStackTrace();
			
			return null;
		}
	}
}