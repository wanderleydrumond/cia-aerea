package services;

import java.io.Serializable;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import daos.UserDAO;
import dtos.UserDTO;
import entities.User;
import enums.Action;
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
	 * <p>The serial version identifier for this class.<p>
	 * 
	 * <p>This identifier is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization.<p>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Object that contains methods from <code>User</code> object to switch it
	 * between Entity and DTO formats.
	 */
	@Inject
	UserMapper userMapper;

	/**
	 * Object that contains all methods to manipulates database regarding users
	 * table.
	 */
	@Inject
	UserDAO userDAO;

	/**
	 * Registers a new user into the system.
	 *
	 * @param userDTO an object that contains the information for the new user
	 * @return true if the user was registered successfully, otherwise false
	 *         <ul>
	 *         	   <li><code>TRUE</code> if the user was registered successfully</li>
	 *         	   <li><code>NULL</code> if error occurred, preventing the user from being saved</li>
	 *         </ul>
	 */
	public Boolean signUp(UserDTO userDTO) {
		try {
			User user = userMapper.toEntity(userDTO);
			user.setRole(Role.CLIENT);

			userDAO.persist(user);

			return true;
		} catch (Exception exception) {
			System.err.println("Catch signUp() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Validates the role of the user with the given token.
	 *
	 * @param token the authorization token of the user
	 * @return the role of the user with the given token, one of the following:
	 * 		<ul>
	 *			<li><strong>EMPLOYEE</strong> if the user is an employee</li>
	 *			<li><strong>ADMINISTRATOR</strong> if the user is an administrator</li>
	 *			<li><strong>CLIENT</strong> if the user is a client</li>
	 *			<li><strong>null</strong> if the user with the given token doesn't exist</li>
	 * 		</ul>
	 */
	public Role validateLoggedUserRole(String token) {
		try {
			User user = userDAO.getUserByToken(token, "token");

			if (user != null) {
				if (user.getRole().equals(Role.EMPLOYEE)) {
					return Role.EMPLOYEE;
				} else if (user.getRole().equals(Role.ADMINISTRATOR)) {
					return Role.ADMINISTRATOR;
				}

				return Role.CLIENT;
			}

			return null;
		} catch (Exception exception) {
			System.err.println("Catch validateLoggedUserRole() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Saves another user in the database.
	 * 
	 * @param userDTOtoBeCreated logged user who will save the given user
	 * @param loggedUserRole 	 logged user role (ADMINISTRATOR or EMPLOYEE)
	 * @param action			 action to be done (CREATE or UPDATE) 
	 * @return
	 * 		  <ul>
	 * 			<li>The DTO for the new created user</li>
	 * 			<li><strong>NULL</strong> if error occurred, preventing the user from being saved</li>
	 * 		  </ul>
	 */
	public UserDTO save(UserDTO userDTOtoBeCreated, Role loggedUserRole, Action action) {
		try {
			User userToBeSaved = userMapper.toEntity(userDTOtoBeCreated);

			if (loggedUserRole.equals(Role.EMPLOYEE)) {
				userToBeSaved.setRole(Role.CLIENT);
				userDTOtoBeCreated.setRole(Role.CLIENT); // apenas para retornar uma informação mais fidedigna
			}

			if (action.equals(Action.CREATE)) {
				// verifica se o username já existe
				Boolean usernameExists = userDAO.exists(userDTOtoBeCreated.getUsername());
				if (Boolean.TRUE.equals(usernameExists)) {
					return new UserDTO();
				}
				
				if (usernameExists == null) {
					return null;
				}
				
				userDAO.persist(userToBeSaved);
				userDTOtoBeCreated.setId(userToBeSaved.getId());
			}

			return userDTOtoBeCreated;
		} catch (Exception exception) {
			System.err.println("Catch save() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Signs in a user into the system.
	 * 
	 * @param username the username of the user
	 * @param password the password of the user
	 * @return
	 * 	<ul>If:
	 * 		<li><strong>Successful</strong>, the generated token</li>
	 * 		<li><strong>Unsuccessful</strong>, null</li>
	 * 	</ul>
	 */
	public String signIn(String username, String password) {
		try {
			User user = userDAO.signIn(username, password);

			if (user == null) {
				return null;
			}

			user.setToken(UUID.randomUUID().toString());
			userDAO.merge(user);

			return user.getToken();
		} catch (Exception exception) {
			System.err.println("Catch signIn() in UserService");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Signs out a user from the system.
	 * 
	 * @param token the token of the user
	 * @return
	 * 	<ul>If:
	 * 		<li><strong>Successful</strong>, true</li>
	 * 		<li><strong>Unsuccessful</strong>, false</li>
	 * 		<li><strong>Error</strong>, null</li>
	 * 	</ul>
	 */
	public Boolean signOut(String token) {
		try {
			Integer amountOfRowsUpdated = userDAO.signOut(token);

			switch (amountOfRowsUpdated) {
			case 0:
				return false;

			case 1:
				return true;
				
			default:
				return null;
			}
		} catch (Exception exception) {
			System.err.println("Catch signOut() in UserService");
			exception.printStackTrace();

			return null;
		}
	}
}