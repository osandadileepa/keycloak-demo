package com.osanda.work.keycloakDemo.core.keycloak.utils;

import java.util.Arrays;


import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.osanda.work.keycloakDemo.core.keycloak.dto.UserCreationDto;
import com.osanda.work.keycloakDemo.core.keycloak.dto.UserInfo;

public class UserRepresentationDto {

	public UserRepresentation getUserRepresentation(UserCreationDto userInfo) {

		UserRepresentation user = new UserRepresentation();
		user.setUsername(userInfo.getUserName());
		user.setEmail(userInfo.getEmail());
		user.setFirstName(userInfo.getFirstName());
		user.setLastName(userInfo.getLastName());
		user.setEnabled(userInfo.getActive());

		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setTemporary(false);
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(userInfo.getPassword());

		user.setCredentials(Arrays.asList(credentialRepresentation));

		return user;
	} // getUserRepresentation()

	public UserInfo toUserInfo(UserRepresentation representation) {
		UserInfo userInfo = new UserInfo();
		userInfo.setSub(representation.getId());
		userInfo.setEmail(representation.getEmail());
		userInfo.setPreferredUsername(representation.getUsername());
		userInfo.setEnabled(representation.isEnabled());
		userInfo.setGivenName(representation.getFirstName());
		userInfo.setFamilyName(representation.getLastName());
		return userInfo;
	} // toUserInfo()

}
