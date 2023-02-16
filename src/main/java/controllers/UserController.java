package controllers;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.UserDTO;
import enums.Action;
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
		
		UserDTO userDTOCreated = userService.save(userDTOtoBeCreated, loggedUserRole, Action.CREATE);
		
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
}