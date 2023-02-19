package controllers;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.UserDTO;
import entities.User;
import enums.Role;
import services.UserService;

/**
 * Class that contains all requisition methods that refers to user.
 * 
 * @author Wanderley Drumond
 *
 */
@Path("/user")
public class UserController {
	/**
	 * Object that contains all user service methods.
	 */
	@Inject
	UserService userService;

   /**
	* Registers a new user in the system.
	*
	* @param userDTO an object containing the information for the new user
	* @return HTTP response with status code:
	*      <ul>
	*         <li><strong>200 (OK)</strong> if the user was registered successfully</li>
	*         <li><strong>400 (Bad Request)</strong> if error occurred, preventing the user from being saved</li>
	*         <li><strong>403 (Forbidden)</strong> if the userDTO is null</li>
	*      </ul>
	*/
	@Path("/signup")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signUp(UserDTO userDTO) {
		if (userDTO == null) {
			return Response.status(403).build();
		}

		Boolean isCreated = userService.signUp(userDTO);

		if (isCreated == null) {
			return Response.status(400).build();
		}

		return Response.ok(userDTO).build();
	}
	
	/**
	 * Creates a new user in the system for a logged user.
	 *
	 * @param token 			 the authorization token of the logged user
	 * @param userDTOtoBeCreated the information for the new user to be created
	 * @return a response with the created user information, with one of the following statuses:
	 * 		<ul>
	 *			<li><strong>200 (OK)</strong> and the created user information in the response body, if the user was created successfully</li>
	 *			<li><strong>403 (Forbidden)</strong> if the token is missing, blank or the logged user is a client</li>
	 *			<li><strong>406 (Not Acceptable)</strong> if the role for the new user is missing</li>
	 *			<li><strong>400 (Bad Request)</strong> if error occurred, preventing the user from being saved</li>
	 * 		</ul>
	 */
	@Path("/create")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response create(@HeaderParam("token") String token, UserDTO userDTOtoBeCreated) {
		if (userDTOtoBeCreated.getRole() == null) {
			return Response.status(406).build();
		}
		Role loggedUserRole = userService.validateLoggedUserRole(token);
		
		if (userDTOtoBeCreated == null || token == null || token.isBlank() || loggedUserRole == null || loggedUserRole.equals(Role.CLIENT)) {
			return Response.status(403).build();
		}
		
		UserDTO userDTOCreated = userService.create(userDTOtoBeCreated, loggedUserRole);
		
		if (userDTOCreated == null) {
			return Response.status(400).build();
		}
		
		if (userDTOCreated.getId() == null) {
			return Response.status(409).build();
		}
		
		return Response.status(201).entity(userDTOCreated).build();
	}
	
	/**
	 * Signs a user into the system.
	 *
	 * @param username the username of the user
	 * @param password the password of the user
	 * @return a response with one of the following statuses:
	 * 		<ul>
	 *			<li><strong>200 (OK)</strong> if the user was signed in successfully</li>
	 *			<li><strong>401 (Unauthorized)</strong> if the username or password is incorrect</li>
	 *			<li><strong>403 (Forbidden)</strong> if the username or password is missing or blank</li>
	 * 		</ul>
	 */
	@Path("/signin")
	@POST
	public Response signIn(@HeaderParam("username") String username, @HeaderParam("password") String password) {
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return Response.status(403).build();
		}
		
		String token = userService.signIn(username, password);
		
		if (token == null) {
			return Response.status(401).build();
		}
		
		return Response.ok().build();
	}
	
	/**
	 * Signs out the logged user from the system.
	 *
	 * @param token the authorization token of the user to be logged out
	 * @return a response with one of the following statuses:
	 * 		<ul>
	 *			<li><strong>200 (OK)</strong> if the user was logged out successfully</li>
	 *			<li><strong>403 (Forbidden)</strong> if the token is missing or blank</li>
	 *			<li><strong>400 (Bad Request)</strong> if error occurred, preventing the user from being logged out</li>
	 * 		</ul>
	 */
	@Path("/signout")
	@POST
	public Response signOut(@HeaderParam("token") String token) {
		if (token == null || token.isBlank()) {
			return Response.status(403).build();
		}
		
		Boolean isLoggedOut = userService.signOut(token);
		
		if (Boolean.FALSE.equals(isLoggedOut)) {
			return Response.status(400).build();
		}
		
		return Response.ok().build();
	}
	
	/**
	 * Updates names and / or role for the user that owns the given id, depending the privileges of the logged user.
	 * 
	 * @param token 			logged user identifier key
	 * @param idUserToBeUpdated user that will be updated primary key
	 * @param userDTO			new informations to be updated
	 * @return
	 * 		  <ul>
	 * 			<li>
	 * 				<strong>403 (Forbidden)</strong> If:
	 * 				<ul>
	 * 					<li>A client try to update another user</li>
	 * 					<li>An employee try to update another one employee</li>
	 * 					<li>An employee try to update an admin</li>
	 * 					<li>An user try to update their own username or password </li>
	 * 					<li>An user try to update username or password of another user</li>
	 * 				</ul>
	 * 			</li>
 	 * 			<li><strong>401 (Unauthorized)</strong></li>
 	 * 			<li><strong>404 (Not Found)</strong> if the logged user or the user who will be updated wasn't found in the database</li>
 	 * 			<li><strong>503 (Service Unavailable)</strong>if there was some issue with the database</li> 
 	 * 			<li><strong>200 (OK)</strong> if the used was successfully updated</li>
	 * 		  </ul>
	 */
	@Path("/update/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response update(@HeaderParam("token") String token, @PathParam("id") Integer idUserToBeUpdated, UserDTO userDTO) {
		String message;
		if (token == null || token.isBlank() || idUserToBeUpdated == null) {
			message = "No token in logged user or no id in user to be updated";
			return Response.status(403).entity(message).build();
		}
		
		Optional<User> loggedUser = userService.getByToken(token);
		Optional<User> userToBeUpdated = userService.getById(userDTO.getId());
		
		// Se houver um utilzador logado 
		if (loggedUser.isPresent()) {
			// E for um cliente
			if (loggedUser.get().getRole().equals(Role.CLIENT)) {
				// E tentar alterar dados de outro cliente 
				if (idUserToBeUpdated != userDTO.getId()) {
					message = "A client cannot update another user";
					return Response.status(403).entity(message).build();
				}
			}
			
			// E se for um empregado
			if (loggedUser.get().getRole().equals(Role.EMPLOYEE)) {
				// E se tentar alterar dados que não sejam dele próprio ou que sejam de outro user que não seja um cliente
				if (!userToBeUpdated.get().getRole().equals(Role.CLIENT) && userToBeUpdated.get().getId() != loggedUser.get().getId()) {
					message = "An employee is only allowed to update their own or clients data";
					return Response.status(403).entity(message).build();
				}
			}
		}
		
		
		// Se não achou o user na base de dados
		if (userToBeUpdated.isEmpty() || loggedUser.isEmpty()) {
			message = "User not found in database";
			return Response.status(404).entity(message).build();
		}
		
		// Se o cliente tentar modificar outro utilizador
		if (loggedUser.get().getRole().equals(Role.CLIENT) && loggedUser.get().getId() != userToBeUpdated.get().getId()) {
			message = "The logged user is a client and is trying to update another user";
			return Response.status(403).entity(message).build();
		}
		
		// Se um funcionário ou um cliente tentar atualizar o role de um utilizador qualquer
		if (loggedUser.get().getRole().equals(Role.CLIENT) || loggedUser.get().getRole().equals(Role.EMPLOYEE) && !userToBeUpdated.get().getRole().equals(userDTO.getRole())) {
			message = "An employee ou a client is trying to update the role of another user";
			return Response.status(403).entity(message).build();
		}
		
		// Se deu algum problema na base de dados
		if (userToBeUpdated == null || loggedUser == null) {
			message = "There where some problem with the database trying to find the following object entities: loggedUser and UserToBeUpdated";
			return Response.status(503).entity(message).build();
		}
		
		UserDTO userDTOToBeUpdated = userService.update(loggedUser.get(), userToBeUpdated.get(), userDTO);
		
		if (userDTOToBeUpdated == null) {
			message = "There where some problem with the database trying to find the following object DTO: userDTOToBeUpdated";
			return Response.status(503).build();
		}
		
		if (userDTOToBeUpdated.getId() == null) {
			message = "Is not possible to update username and password";
			return Response.status(403).entity(message).build();
		}
		
		return Response.ok(userDTOToBeUpdated).build();
	}
}