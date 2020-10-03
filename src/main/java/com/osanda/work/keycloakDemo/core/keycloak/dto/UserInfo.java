package com.osanda.work.keycloakDemo.core.keycloak.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.json.StringOrArrayDeserializer;
import org.keycloak.json.StringOrArraySerializer;
import org.keycloak.representations.AddressClaimSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserInfo implements Serializable {

	private static final long serialVersionUID = 500218240486185505L;

	// Should be in signed UserInfo response
	@JsonProperty("iss")
	protected String issuer;
	@JsonProperty("aud")
	@JsonSerialize(using = StringOrArraySerializer.class)
	@JsonDeserialize(using = StringOrArrayDeserializer.class)
	protected String[] audience;

	@JsonProperty("sub")
	protected String sub;

	@JsonProperty("name")
	protected String name;

	@JsonProperty("given_name")
	protected String givenName;

	@JsonProperty("family_name")
	protected String familyName;

	@JsonProperty("middle_name")
	protected String middleName;

	@JsonProperty("nickname")
	protected String nickName;

	@JsonProperty("preferred_username")
	protected String preferredUsername;

	@JsonProperty("profile")
	protected String profile;

	@JsonProperty("picture")
	protected String picture;

	@JsonProperty("website")
	protected String website;

	@JsonProperty("email")
	protected String email;

	@JsonProperty("email_verified")
	protected Boolean emailVerified;

	@JsonProperty("gender")
	protected String gender;

	@JsonProperty("birthdate")
	protected String birthdate;

	@JsonProperty("zoneinfo")
	protected String zoneinfo;

	@JsonProperty("locale")
	protected String locale;

	@JsonProperty("phone_number")
	protected String phoneNumber;

	@JsonProperty("phone_number_verified")
	protected Boolean phoneNumberVerified;

	@JsonProperty("address")
	protected AddressClaimSet address;

	@JsonProperty("updated_at")
	protected Long updatedAt;

	@JsonProperty("claims_locales")
	protected String claimsLocales;

	@JsonProperty("enabled")
	protected Boolean enabled;

	protected Map<String, Object> otherClaims = new HashMap<>();

	@JsonIgnore
	public String[] getAudience() {
		return audience;
	}

	public boolean hasAudience(String audience) {
		for (String a : this.audience) {
			if (a.equals(audience)) {
				return true;
			}
		}
		return false;
	}

	public UserInfo(UserCreationDto user) {
		this.preferredUsername = user.getUserName();
		this.email = user.getEmail();
		this.familyName = user.getLastName();
		this.givenName = user.getFirstName();
		this.enabled = user.getActive();
		this.sub = user.getSub();
	}

}
