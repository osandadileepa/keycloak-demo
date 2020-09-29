package com.osanda.work.keycloakDemo.core.keycloak.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserCreationDto implements Serializable {

	private static final long serialVersionUID = -6036888803441164888L;

	private String userName;

	private String email;

	private String password;

	private String firstName;

	private String lastName;

	private Boolean active;

	private List<String> roles;

	// info regarding keycloak
	private String sub;

}
