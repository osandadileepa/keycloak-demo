package com.osanda.work.keycloakDemo.core.keycloak.utils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/***
 * @author Randika Hapugoda
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Credential implements Serializable {

	private static final long serialVersionUID = -5187612442269650013L;

	private String username;

	private String email;

	private String password;
}