package com.osanda.work.keycloakDemo.core.keycloak;

import java.util.Optional;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.osanda.work.keycloakDemo.core.keycloak.dto.UserInfo;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.KeycloakUserProfileException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserDeactiveException;
import com.osanda.work.keycloakDemo.core.keycloak.exceptions.UserNamePasswordIncorrectException;
import com.osanda.work.keycloakDemo.core.keycloak.utils.Credential;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***
 * communicate with keycloak server and get token and user releated information
 * 
 * @author Osanda Wedamulla
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

	@Value("${keycloak.resource}")
	private String keyclockClientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.auth-server-url}")
	private String authServerUrl;

	/***
	 * initiate request data with embedded credentials
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param credential
	 * @return
	 * @throws Exception
	 */
	public Optional<AccessTokenResponse> getTokenForCredential(Credential credential) throws Exception {

		String userName = credential.getUsername();

		if (userName == null)
			userName = credential.getEmail();

		log.info("Login initiated for {}", userName);

		MultiValueMap<String, String> objectMap = new LinkedMultiValueMap<String, String>();
		objectMap.add("username", userName);
		objectMap.add("password", credential.getPassword());
		objectMap.add("client_id", this.keyclockClientId);
		objectMap.add("grant_type", "password");

		if (!this.clientSecret.isEmpty()) {
			objectMap.add("client_secret", this.clientSecret);
		}

		Optional<AccessTokenResponse> token = this.exchange(objectMap);

//		if (token.isPresent()) {
//			UserInfo userInfo = this.getUserInformationFromToken(token.get());
//		}
//		// token availability
//		else {
//			throw new Exception();
//		}

		return token;
	} // getTokenForCredential()

	private final String BAD_REQEST = "400 Bad Request";

	/***
	 * get access token from keycloak server for valid credentials
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param params
	 * @return @AccessTokenResponse
	 */
	private Optional<AccessTokenResponse> exchange(MultiValueMap<String, String> params) throws Exception {
		RestTemplate template = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Accept", "application/json");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);

		ResponseEntity<AccessTokenResponse> token = null;
		String url = String.format("%s/realms/%s/protocol/openid-connect/token", this.authServerUrl, this.realm);

		try {
			token = template.postForEntity(url, request, AccessTokenResponse.class);
			return Optional.of(token.getBody());
		} catch (Exception e) {
			log.error("Error connecting to Keyclok {} ", e.getMessage());

			if (e.getMessage().equals(BAD_REQEST)) {
				throw new UserDeactiveException();
			}
			throw new UserNamePasswordIncorrectException();
		}
	} // exchange()

	/***
	 * get available user information form keycloak
	 * 
	 * @author Osanda Wedamulla
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	private UserInfo getUserInformationFromToken(AccessTokenResponse token) throws Exception {

		if (token == null) {
			return null;
		}

		RestTemplate template = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Authorization", "Bearer " + token.getToken());

		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		String userInfoUrl = String.format("%s/realms/%s/protocol/openid-connect/userinfo", authServerUrl, realm);

		ResponseEntity<UserInfo> userInfo = null;

		try {
			userInfo = template.postForEntity(userInfoUrl, entity, UserInfo.class);
		} catch (Exception e) {
			log.error("Error connection with keycloak to get profile {}", e.getMessage());
			throw new KeycloakUserProfileException();
		}

		UserInfo user = null;

		if (userInfo != null) {
			user = userInfo.getBody();
			log.info("User Information receving complete for user : {}", user.getPreferredUsername());
		}

		return user;

	}// getUserInformationFromToken()

}// KeycloakService {}