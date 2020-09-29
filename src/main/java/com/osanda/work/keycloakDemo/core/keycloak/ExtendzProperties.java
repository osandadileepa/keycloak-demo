package com.osanda.work.keycloakDemo.core.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/***
 * Configurations properties for extendz
 * 
 * @author Randika Hapugoda
 *
 */
@Data
@ConfigurationProperties(prefix = ExtendzProperties.EXTENDZ_PREFIX)
public class ExtendzProperties {

    public static final String EXTENDZ_PREFIX = "extendz";

    private boolean enabled = true;

    public String modelMetaEndpoint;

    /***
     * Directory to save the data
     */
    public String localDataDir;

    /*** Entity name for the maintaining the user profile */
    public String userProfileUrl = "/api/users";
}

