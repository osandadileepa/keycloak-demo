package com.osanda.work.keycloakDemo.core.keycloak;
	
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.osanda.work.keycloakDemo.core.keycloak.dto.UserInfoResource;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserAlreadyExistsException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserCreationFailedException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserDeletionFailedException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.data.rest.base-path}/keycloak/")
public class KeycloakAdminController {

	private final KeycloakAdminService keycloakAdminService;
	private final UserRepresentationResourceAssembler assembler;
	//private final LoginService loginService;

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
			return ResponseEntity.ok(this.assembler.toResource(userRepresentation));
		} catch (UserAlreadyExistsException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(
					String.format("%s User Name already exists use a different username : ", userDto.getUserName()));
		} catch (UserCreationFailedException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("User sign up failed.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Sign Up process failed.");
		}

	} // createUser()

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
	public PagedResources<UserInfoResource> getUsers(@RequestParam(required = false, name = "userName") String userName,
			Pageable pageable, PagedResourcesAssembler<UserInfo> pagedAssembler) {

		Page<UserInfo> users = keycloakAdminService.getUsers(userName, pageable);

		return pagedAssembler.toResource(users, assembler);
	} // getUsers()

	@GetMapping("users/{id}")
	public ResponseEntity<?> getUser(@PathVariable("id") String id) {
		UserInfo userInfo = keycloakAdminService.getUser(id);
		return ResponseEntity.ok(this.assembler.toResource(userInfo));
	} // getUser()

//	/**
//	 * @author Osanda Wedamulla
//	 * 
//	 * @param user
//	 * @return
//	 */
//	@PatchMapping("users")
//	public ResponseEntity<?> updateUser(@RequestBody UserDto user) {
//
//		UserInfo userInfo = this.keycloakAdminService.updateUserDetails(user);
//
//		if (userInfo != null)
//			this.loginService.updateLocationUser(userInfo);
//
//		return ResponseEntity.ok(user.getUserName() + " : details updated.");
//	}// putUser()

//	@DeleteMapping("users/{id}")
//	public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
//		try {
//
//			String sub = keycloakAdminService.deleteUser(id);
//			if (sub != null) {
//				this.loginService.deleteLocalUserFromSub(sub);
//			}
//			return ResponseEntity.status(204).build();
//		} catch (UserDeletionFailedException e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//		}
//	} // deleteUser()

	@GetMapping(value = "user-roles/{id}")
	public ResponseEntity<?> getUserRoles(@PathVariable("id") String id) {

		return ResponseEntity.ok(this.keycloakAdminService.getAvilableUserRolesFromKeycloak(id));

	}// getUserRoles()

}// KeycloakAdminController()