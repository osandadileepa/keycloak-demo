package com.osanda.work.keycloakDemo.core.keycloak.dto;

import java.io.Serializable;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class UserUpdateDto implements Serializable {

	private static final long serialVersionUID = -8090925290149754629L;
	
	private String id;

	private String userName;

	private String firstName;

	private String lastName;

	private Boolean active;

	private String email;

	private Set<String> updatedRoles;
	
	private String password;

}
