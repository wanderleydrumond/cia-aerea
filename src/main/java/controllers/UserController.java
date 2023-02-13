package controllers;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dtos.UserDTO;
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
	*         <li><strong>200: OK</strong> if the user was registered successfully</li>
	*         <li><strong>400: Bad Request</strong> if error occurred, preventing the user from being saved</li>
	*         <li><strong>403: Forbidden</strong> if the userDTO is null</li>
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
}