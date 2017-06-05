package org.tinywind;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author tinywind
 * @since 2017-06-04
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakToken {
    private String accessToken;
    private Integer expiresIn;
    private Integer refreshExpiresIn;
    private String refreshToken;
    private String tokenType;
    private String idToken;
    private String sessionState;

    private IdToken id;
}
