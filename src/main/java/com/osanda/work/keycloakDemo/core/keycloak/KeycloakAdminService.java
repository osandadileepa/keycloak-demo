package com.osanda.work.keycloakDemo.core.keycloak;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.osanda.work.keycloakDemo.core.keycloak.dto.UserCreationDto;
import com.osanda.work.keycloakDemo.core.keycloak.dto.UserInfo;
import com.osanda.work.keycloakDemo.core.keycloak.dto.UserUpdateDto;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserAlreadyExistsException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserCreationFailedException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserDeletionFailedException;
import com.osanda.work.keycloakDemo.core.keycloak.utils.UserRepresentationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * operation to get user details and do modifications
 * 
 * @author Osanda Wedamulla
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "keycloak", name = "auth-server-url")
public class KeycloakAdminService {

	@Value("${keycloak.resource}")
	private String keyclockClientId;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	@Value("${keycloak.resource}")
	private String clientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	@Value("${credentials..keycloak.admin}")
	private String adminUsername;

	@Value("${credentials.keycloak.password}")
	private String adminPassword;

	private Keycloak getKeyCloak() {
		return KeycloakBuilder.builder().serverUrl(authServerUrl).realm("master").username(adminUsername)
				.password(adminPassword).clientId("admin-cli")
				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
	} // getKeyCloak()

	private UsersResource getKeycloakUserResource() {
		return getRealmResource().users();
	} // getKeycloakUserResource()

	private RealmResource getRealmResource() {
		return getKeyCloak().realm(realm);
	}// getRealmResource()

	public Optional<UserRepresentation> getUserByEmail(String email) {
		UsersResource userRessource = getKeycloakUserResource();
		List<UserRepresentation> searched = userRessource.search(email, 0, 1);
		UserRepresentation u = null;
		if (searched.size() > 0)
			u = searched.get(0);
		return Optional.ofNullable(u);
	}

	public String deleteUser(String id) throws UserDeletionFailedException {
		UsersResource userRessource = getKeycloakUserResource();
		Response deleteResponse = userRessource.delete(id);
		int statusId = deleteResponse.getStatus();
		if (statusId != 204)
			throw new UserDeletionFailedException();
		else
			return id;
	} // deleteUser()

	public UserInfo getUserByUserName(String userName) {
		UsersResource userRessource = getKeycloakUserResource();
		List<UserRepresentation> search = userRessource.search(userName);
		return search.stream().findFirst().map(rep -> new UserRepresentationDto().toUserInfo(rep)).get();
	}// getUserByUserName()

	/**
	 * get users from keycloak with pagination
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param userName
	 * @param pageable
	 * @return
	 */
	public List<UserInfo> getUsers(String userName) {

		UsersResource userRessource = getKeycloakUserResource();

		List<UserInfo> userInfos = new ArrayList<>();

		if (userName == null) {

			userInfos = userRessource.list().stream().map(rep -> new UserRepresentationDto().toUserInfo(rep))
					.collect(Collectors.toList());
		} else {

			List<UserRepresentation> search = userRessource.search(userName);

			userInfos = search.stream().map(rep -> new UserRepresentationDto().toUserInfo(rep))
					.collect(Collectors.toList());
		}

		return userInfos;

	}// getUsers()

	/**
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param UserCreationDto userInfo
	 * @return UserInfo
	 */
	public UserInfo createUser(UserCreationDto userInfo) throws Exception {

		UsersResource userRessource = getKeycloakUserResource();

		UserRepresentation user = new UserRepresentationDto().getUserRepresentation(userInfo);
		// Create user
		Response result = userRessource.create(user);
		int statusId = result.getStatus();

		// Created status
		if (statusId == 201) {
			String userId = result.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

			log.info("User created " + userInfo.getUserName() + ". Adding client roles.");

			List<RoleRepresentation> roleRepresentationList = new ArrayList<>();

			if (userInfo.getRoles() != null && userInfo.getRoles().size() > 0) {
				userInfo.getRoles().stream()
						.forEach(r -> roleRepresentationList.add(this.getRoleRepresentaionByRole(r)));
			}

			userRessource.get(userId).roles().clientLevel(getClientByName().getId()).add(roleRepresentationList);

			log.info("Role adding complete for User : " + userInfo.getUserName() + " with, Roles : "
					+ roleRepresentationList.toString());

			userInfo.setSub(userId);

			return new UserInfo(userInfo);

		} else if (statusId == 409) {
			throw new UserAlreadyExistsException();
		} else {
			throw new UserCreationFailedException();
		}
	}// createUser()

	private RoleRepresentation getRoleRepresentaionByRole(String role) {
		return this.getRealmResource().clients().get(getClientByName().getId()).roles().get(role).toRepresentation();
	} // getRoleRepresentaionByRole()

	private ClientRepresentation getClientByName() {
		return this.getRealmResource().clients().findByClientId(clientId).get(0);
	}// getClientByName()

	/**
	 * get user deatils avialable in the keycloak from id
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param id
	 * @return
	 */
	public UserInfo getUser(String id) {
		UsersResource userRessource = getKeycloakUserResource();
		UserRepresentation representation = userRessource.get(id).toRepresentation();
		return new UserRepresentationDto().toUserInfo(representation);
	} // getUser

	/***
	 * reset password with new password
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param newPassword
	 * @param userId
	 */
	public void resetPassword(String userId, String newPassword) {

		UsersResource userResource = getKeycloakUserResource();

		// Define password credential
		CredentialRepresentation passwordCred = new CredentialRepresentation();
		passwordCred.setTemporary(false);
		passwordCred.setType(CredentialRepresentation.PASSWORD);
		passwordCred.setValue(newPassword.toString().trim());

		// Set password credential
		userResource.get(userId).resetPassword(passwordCred);

		log.info("Password changed : " + userId);

	}// resetPassword()

	/**
	 * add roles to specific user with userId
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param userId
	 * @param roles
	 */
	public void addRolesToUser(String userId, Set<String> roles) {

		UsersResource userResource = getKeycloakUserResource();

		List<RoleRepresentation> roleRepresentationList = new ArrayList<>();

		if (roles != null && roles.size() > 0)
			roles.stream().forEach(r -> roleRepresentationList.add(this.getRoleRepresentaionByRole(r)));

		List<String> avialableRoles = userResource.get(userId).roles().clientLevel(getClientByName().getId()).listAll()
				.stream().map(r -> r.getName()).collect(Collectors.toList());

		List<String> removeRoles = new ArrayList<>();

		avialableRoles.forEach(r -> {
			if (!roles.contains(r))
				removeRoles.add(r);
		});

		if (removeRoles.size() > 0) {
			List<RoleRepresentation> removeList = removeRoles.stream().map(r -> this.getRoleRepresentaionByRole(r))
					.collect(Collectors.toList());
			userResource.get(userId).roles().clientLevel(getClientByName().getId()).remove(removeList);
			log.info("Removing roles " + removeRoles.toString());
		}

		userResource.get(userId).roles().clientLevel(getClientByName().getId()).add(roleRepresentationList);

		log.info("Roles Updated : " + userId);

	}// addRolesToUser()

	/**
	 * update user information in keycloak
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param id
	 * @param userRepresentation
	 * @return
	 */
	public UserInfo putUser(String id, UserRepresentation userRepresentation) {
		UsersResource userRessource = getKeycloakUserResource();
		userRessource.get(id).update(userRepresentation);
		userRepresentation.setId(id);
		return new UserRepresentationDto().toUserInfo(userRepresentation);
	}// putUser()

	/***
	 * @author Osanda Wedamulla
	 * 
	 * @param userDto
	 * @return
	 */
	public UserInfo updateUserDetails(UserUpdateDto userDto) {

		String id = userDto.getId();

		UserRepresentation user = new UserRepresentation();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setEnabled(userDto.getActive());

		if (userDto.getPassword() != null)
			this.resetPassword(id, userDto.getPassword());

		if (userDto.getUpdatedRoles() != null && userDto.getUpdatedRoles().size() > 0)
			this.addRolesToUser(id, userDto.getUpdatedRoles());

		return this.putUser(id, user);

	}// updateUserDetails()

	/***
	 * get avilable roles for a user
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> getAvilableUserRolesFromKeycloak(String userId) {

		Set<String> roles = new HashSet<>();

		UsersResource userResource = getKeycloakUserResource();

		userResource.get(userId).roles().clientLevel(getClientByName().getId()).listAll().forEach(r -> {
			roles.add(r.getName());
		});

		return roles;

	}// getAvilableUserRolesFromKeycloak()

} // KeycloakAdminService {}