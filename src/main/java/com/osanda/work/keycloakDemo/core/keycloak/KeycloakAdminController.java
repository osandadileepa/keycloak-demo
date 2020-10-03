package com.osanda.work.keycloakDemo.core.keycloak;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.osanda.work.keycloakDemo.core.keycloak.dto.UserCreationDto;
import com.osanda.work.keycloakDemo.core.keycloak.dto.UserInfo;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserAlreadyExistsException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserCreationFailedException;
import com.osanda.work.keycloakDemo.utils.ResponseMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.data.rest.base-path}/keycloak/")
public class KeycloakAdminController {

	private final KeycloakAdminService keycloakAdminService;
	
	// private final LoginService loginService;

	/***
	 * create new user in the keycloak and give the response accordingly
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param userDto
	 * @return ResponseEntity
	 */
	@PostMapping("users")
	public ResponseEntity<?> createUser(@RequestBody UserCreationDto userDto) {

		try {
			UserInfo userRepresentation = keycloakAdminService.createUser(userDto);
			return ResponseEntity.ok(userRepresentation);
		} catch (UserAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseMessage.getError(
					String.format("%s User Name already exists use a different username : ", userDto.getUserName())));
		} catch (UserCreationFailedException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseMessage.getError("User sign up failed."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseMessage.getError("Sign Up process failed."));
		}

	} // createUser()
	
	@PatchMapping("users/{id}")
	public ResponseEntity<?> resetUserPassword(@PathVariable("id") String id,
			@RequestParam(name = "newPassword") String newPassword) {

		keycloakAdminService.resetPassword(id, newPassword);
		return ResponseEntity.ok(ResponseMessage.createMessage("Password reset for user id : " + id));
	} // getUser()
	
	
	//==================additional keycloak information==========================//

	/***
	 * get all the users in the keycloak with pagination
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param userName
	 * @param pageable
	 * @param pagedAssembler
	 * @return PagedResources<UserInfoResource>
	 */
	@GetMapping("users")
	public List<UserInfo> getUsers(@RequestParam(required = false, name = "userName") String userName) {

		List<UserInfo> users = keycloakAdminService.getUsers(userName);

		return users;
	} // getUsers()

	@GetMapping("users/{id}")
	public ResponseEntity<?> getUser(@PathVariable("id") String id) {
		UserInfo userInfo = keycloakAdminService.getUser(id);
		return ResponseEntity.ok(userInfo);
	} // getUser()

	@GetMapping(value = "user-roles/{id}")
	public ResponseEntity<?> getUserRoles(@PathVariable("id") String id) {

		return ResponseEntity.ok(this.keycloakAdminService.getAvilableUserRolesFromKeycloak(id));
	}// getUserRoles()
	
}// KeycloakAdminController()