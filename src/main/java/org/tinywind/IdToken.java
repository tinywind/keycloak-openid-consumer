package org.tinywind;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

/**
 * @author tinywind
 * @since 2017-06-04
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdToken {
    private String alg;
    private Map<String, Object> address;
    private String aud;
    private String azp;
    private String email;
    private Boolean emailVerified;
    private Long exp;
    private String familyName;
    private String givenName;
    private Long iat;
    private String iss;
    private String jti;
    private String name;
    private Long nbf;
    private String preferredUsername;
    private String roles;
    private String sessionState;
    private String sub;
    private String typ;
}
