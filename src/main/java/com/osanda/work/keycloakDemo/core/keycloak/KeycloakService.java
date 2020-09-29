package com.osanda.work.keycloakDemo.core.keycloak;

import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
 * @author Randika Hapugoda
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

//	private final LoginService loginService;
//	private final UserRepository userRepository;

	/***
	 * initiate request data with embedded credentials
	 * 
	 * @author Randika Hapugoda
	 * @author Osanda Wedamulla
	 * 
	 * @param credential
	 * @return
	 * @throws Exception
	 */
	public Optional<AccessTokenResponse> getTokenForCredential(Credential credential, HttpServletRequest request)
			throws Exception {
		log.info("Login initiated for {}", credential.getEmail());

		MultiValueMap<String, String> objectMap = new LinkedMultiValueMap<String, String>();
		objectMap.add("username", credential.getEmail());
		objectMap.add("password", credential.getPassword());
		objectMap.add("client_id", this.keyclockClientId);
		objectMap.add("grant_type", "password");

		if (!this.clientSecret.isEmpty()) {
			objectMap.add("client_secret", this.clientSecret);
		}

		Optional<AccessTokenResponse> token = this.exchange(objectMap);

		if (token.isPresent()) {
			
			
			UserInfo userInfo = this.getUserInformationFromToken(token.get());

//			User user = this.userRepository.findByUserName(credential.getEmail());
//
//			if (user == null)
//				user = this.userRepository.findByEmail(credential.getEmail());
//
//			if (user != null) {
//
//				if (user.getExpireDate() != null) {
//					if (!user.getExpireDate().isAfter(LocalDate.now().minusDays(1)))
//						throw new UserExpiredException();
//				} else {
//
//					LocalDate expDate = LocalDate.now().plusDays(14);
//					user.setExpireDate(expDate);
//					log.info("Expire date set {} for user : {}", expDate, user.getUserName());
//
//					try {
//						this.userRepository.save(user);
//						log.info("User Updated : " + user.getUserName());
//					} catch (DataIntegrityViolationException e) {
//						log.error("Error updating user", e.getMessage());
//					}
//
//				}
//
//			}
//			// create local user if user not available localy
//			else {
//
//				
//
//				user = new User();
//
//				user.setId(userInfo.getSub());
//				user.setUserName(userInfo.getPreferredUsername());
//				user.setEmail(userInfo.getEmail());
//				user.setFirstName(userInfo.getGivenName());
//				user.setLastName(userInfo.getFamilyName());
//				user.setActive(true);
//
//				if (user != null && user.getExpireDate() == null) {
//
//					LocalDate expDate = LocalDate.now().plusDays(14);
//					user.setExpireDate(expDate);
//					log.info("Expire date set {} for user : {}", expDate, user.getUserName());
//				}
//
//				try {
//					this.userRepository.save(user);
//					log.info("New user created : " + userInfo.getPreferredUsername());
//				} catch (DataIntegrityViolationException e) {
//					log.error("Error saving user", e.getMessage());
//				}
//
//			}
			
			
		} 
		
		// token availability
		else {
			throw new Exception();
		}

		return token;
	} // getTokenForCredential()

	private final String BAD_REQEST = "400 Bad Request";

	/***
	 * get access token from keycloak server for valid credentials
	 * 
	 * @author Randika Hapugoda
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

//	/***
//	 * get user profile from access token and authentication
//	 * 
//	 * @author Osanda Wedamulla
//	 * 
//	 * @param token
//	 * @param authentication
//	 * @return
//	 * @throws Exception
//	 */
//	public UserDto getUserProfileFromTokenAndAuthentication(Authentication authentication) throws Exception {
//
//		User user = this.loginService.updateRolesWithKeycloakFromAuthentication(authentication);
//
//		return new UserDto(user);
//
//	}// getUserProfileFromTokenAndAuthentication()

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