package com.osanda.work.keycloakDemo.core;

import java.util.Optional;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.osanda.work.keycloakDemo.core.keycloak.KeycloakService;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.KeycloakUserProfileException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserDeactiveException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserNamePasswordIncorrectException;
import com.osanda.work.keycloakDemo.core.keycloak.utils.Credential;
import com.osanda.work.keycloakDemo.utils.ResponseMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.data.rest.base-path}/login")
public class LoginController {

	private final KeycloakService keycloakService;

	/***
	 * handle all the login senarios
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param credential
	 * @return
	 */
	@PostMapping()
	private ResponseEntity<?> loginWithCredential(@RequestBody Credential credential) {

		try {

			Optional<AccessTokenResponse> tokenForCredential = this.keycloakService.getTokenForCredential(credential);

			if (tokenForCredential.isPresent()) {
				return ResponseEntity.ok(tokenForCredential.get());
			}
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
					.body(ResponseMessage.getError("Login is Prohabitetd."));

		} catch (UserDeactiveException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ResponseMessage.getError(String.format("%s : User is not Active !!", credential.getEmail())));

		} catch (UserNamePasswordIncorrectException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ResponseMessage.getError("Username or Password is incorrect."));

		} catch (KeycloakUserProfileException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage
					.getError(String.format("%s : UserProfile initialization failed.", credential.getEmail())));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ResponseMessage.getError("Error authenticating user."));
		}

	}// loginWithCredential

}