package services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
	private UserMapper userMapper;

	/**
	 * Object that contains all methods to manipulates database regarding users table.
	 */
	@Inject
	private UserDAO userDAO;
	
	/**
	 * Object that contains all ticket service methods.
	 */
	@Inject
	private TicketService ticketService;

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
			user.setIsDeleted(false);

			userDAO.persist(user);

			return true;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in signUp() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Validates the role of the user with the given token.
	 *
	 * @param token logged user identifier key
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
			Optional<User> user = userDAO.findByToken(token);

			if (user.isPresent()) {
				if (user.get().getRole().equals(Role.EMPLOYEE)) {
					return Role.EMPLOYEE;
				} else if (user.get().getRole().equals(Role.ADMINISTRATOR)) {
					return Role.ADMINISTRATOR;
				}

				return Role.CLIENT;
			}

			return null;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in validateLoggedUserRole() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Saves another user in the database.
	 * 
	 * @param userDTOtoBeSaved logged user who will save the given user
	 * @param loggedUserRole   logged user role (ADMINISTRATOR or EMPLOYEE) 
	 * @return
	 * 		  <ul>
	 * 			<li>The DTO for the new created user</li>
	 * 			<li><strong>NULL</strong> if error occurred, preventing the user from being saved</li>
	 * 		  </ul>
	 */
	public UserDTO create(UserDTO userDTOtoBeSaved, Role loggedUserRole) {
		try {
			User userToBeSaved = userMapper.toEntity(userDTOtoBeSaved);

			if (loggedUserRole.equals(Role.EMPLOYEE)) {
				userToBeSaved.setRole(Role.CLIENT);
				userDTOtoBeSaved.setRole(Role.CLIENT); // apenas para retornar uma informação mais fidedigna
			}

			// verifica se o username já existe
			Boolean usernameExists = userDAO.exists(userDTOtoBeSaved.getUsername());
			if (Boolean.TRUE.equals(usernameExists)) {
				return new UserDTO();
			}
			
			if (usernameExists == null) {
				return null;
			}
			
			userToBeSaved.setIsDeleted(false);
			userDAO.persist(userToBeSaved);
			userDTOtoBeSaved.setId(userToBeSaved.getId());
			
			return userDTOtoBeSaved;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in save() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Updates user's data into database.
	 * 
	 * @param loggedUser	  the user who will do the action
	 * @param userToBeUpdated the user who will suffer the action
	 * @param userDTO		  the data to be updated
	 * @return 
	 * 		  <ul>
	 * 			<li>a new user DTO if user tried to update username or password</li>
	 * 			<li>the user's DTO data updated if the request was done successfully</li>
	 * 			<li>null, if error occurred, preventing the user from being updated</li>
	 * 		  </ul>
	 */
	public UserDTO update(User loggedUser, User userToBeUpdated, UserDTO userDTO) {
		try {
			userToBeUpdated.setId(userDTO.getId());
			userToBeUpdated.setName(userDTO.getName());
			
			if (!userToBeUpdated.getUsername().equals(userDTO.getUsername()) || 
				!userToBeUpdated.getPassword().equals(userDTO.getPassword())) {
				return new UserDTO();
			}
			
			if (loggedUser.getRole().equals(Role.ADMINISTRATOR)) {
				userToBeUpdated.setRole(userDTO.getRole());
			}
			
			userDAO.merge(userToBeUpdated);
			
			return userMapper.toDTO(userToBeUpdated);
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in update() in UserService");
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
			System.err.println("Catch " + exception.getClass().getName() + " in signIn() in UserService");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Signs out a user from the system.
	 * 
	 * @param token logged user identifier key
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
			System.err.println("Catch " + exception.getClass().getName() + " in signOut() in UserService");
			exception.printStackTrace();

			return null;
		}
	}

	/**
	 * Gets the user that owns the given id.
	 * 
	 * @param idUserToBeFound primary key of the user that will be found
	 * @return
	 * 		  <ul>
	 * 			<li>The user, encapsulated into an <code>Optional</code> object</li>
	 * 			<li>Null, if error occurred, preventing the user from being found</li>
	 * 		  </ul>
	 */
	public Optional<User> getById(Integer idUserToBeFound) {
		try {
			Optional<User> optionalUser = userDAO.find(idUserToBeFound);
			
			if (optionalUser == null) { // Se deu algum problema na base de dados
				return null;
			}
			
			if (optionalUser.equals(Optional.empty())) { // Caso não tenha encontrado o utilizador
				return Optional.empty();
			}
			
			return optionalUser;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in getById() in UserService");
			exception.printStackTrace();
			
			return null;
		}
	}

	/**
	 * Gets the user that owns the given token.
	 * 
	 * @param token logged user identifier key
	 * @return
	 */
	public Optional<User> getByToken(String token) {
		return userDAO.findByToken(token);
	}
	
	/**
	 * Gets the role of the logged user
	 * 
	 * @param token logged user identifier key
	 * @return
	 * 		  <ul>
	 * 			<li>null, if:</li>
	 * 				<ul>
	 * 					<li>logged user was not found in database</li>
	 * 					<li>any error occurs in database</li>
	 * 				</ul>
	 * 			<li>Role of the logged user</li>
	 * 		  </ul>
	 */
	public Role getRoleLoggedUser(String token) {
		try {
			Optional<User> optionalUser = getByToken(token);
			if (optionalUser.isEmpty()) {
				return null;
			}
			
			return optionalUser.get().getRole();
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in getRoleLoggedUser() in UserService");
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets all users from system without restrictions.
	 * 
	 * @return A list of all users of the system
	 */
	public List<UserDTO> getAll() {
		try {
			List<User> users = userDAO.findAll();
			List<UserDTO> usersDTO = new ArrayList<>();
			usersDTO = users.stream().map(userMapper::toDTO).collect(Collectors.toList());
			
			return usersDTO;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in getAll() in UserService");
			exception.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Gets all users non deleted by the given role.
	 * 
	 * @param role from the user that is doing the search
	 * @return
	 * 		  <ul>
	 * 			If the user role is:
	 * 			<li><strong>CLIENT</strong>, null</li>
	 * 			<li><strong>EMPLOYEE</strong>, the list of all non-deleted clients</li>
	 * 			<li><strong>ADMINISTRATOR</strong>, the list of all non-deleted users</li>
	 * 		  </ul>
	 */
	public List<UserDTO> getAllNonDeletedByRole(Role role) {
		List<User> users;
		List<UserDTO> usersDTO = null;
		try {
			// Se o usuário logado for um empregado, ele só pode ver lista de clientes
			if (role.equals(Role.EMPLOYEE)) {
				users = userDAO.findAllNonDeletedByRole(Role.CLIENT);
				usersDTO = users.stream().map(userMapper::toDTO).collect(Collectors.toList());
			}
			
			// Se o usuário logado for um cliente, ele não tem nada
			if (role.equals(Role.CLIENT)) {
				usersDTO = null;
			}
			
			return usersDTO;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in getAllByRole() in UserService");
			exception.printStackTrace();
			return null;
		}
	}

	/**
	 * Soft deletes an user and their tickets.
	 * 
	 * @param user to be deleted
	 * @return
	 * 		  <ul>
	 * 			<li>new empty <code>UserDTO</code> if amount of non cancelled tickets with date in the future was found</li>
	 * 			<li><code>UserDTO</code> with all updated data if the user was successfully soft deleted</li>
	 * 			<li>null, if occurs any errors in database</li>
	 * 		  </ul>
	 */
	public UserDTO softDelete(User user) {
		try {
			Integer amountNonCancelledTicketsWithFutureFlight = ticketService.getAllNonDeletedWithFutureFlightByUserId(user.getId());
			
			if (amountNonCancelledTicketsWithFutureFlight > 0) {
				return new UserDTO();
			}
			
			user.setIsDeleted(true);
			
			userDAO.merge(user);
			
			UserDTO userDTO = userMapper.toDTO(user);
			
			ticketService.softDeleteByUserId(user.getId());
			
			return userDTO;
		} catch (Exception exception) {
			System.err.println("Catch " + exception.getClass().getName() + " in softDelete() in UserService");
			exception.printStackTrace();
			
			return null;
		}
	}
}