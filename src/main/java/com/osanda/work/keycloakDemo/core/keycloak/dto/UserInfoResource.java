package com.osanda.work.keycloakDemo.core.keycloak.dto;

import org.springframework.hateoas.Resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResource extends Resources<UserInfo> {

	private String sub;
	private String preferred_username;
	private String email;
	private Boolean enabled;
	private String firstName;
	private String lastName;
	private String profile;

	public UserInfoResource(UserInfo userInfo) {
		this.sub = userInfo.getSub();
		this.preferred_username = userInfo.getPreferredUsername();
		this.email = userInfo.getEmail();
		this.enabled = userInfo.getEnabled();
		this.firstName = userInfo.getGivenName();
		this.lastName = userInfo.getFamilyName();
	}

}
