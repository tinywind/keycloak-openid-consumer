package org.tinywind;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;
import java.util.Random;

/**
 * @author tinywind
 */
@Controller
@SpringBootApplication
public class Launcher {

    private static final Log log = LogFactory.getLog(Launcher.class);

    private static final String SERVICE_DOMAIN;
    private static final String KEYCLOAK_DOMAIN;
    private static final String KEYCLOAK_REALM;
    private static final String KEYCLOAK_CLIENT_ID;
    private static final String KEYCLOAK_CLIENT_SECRET;

    private static final String KEYCLOAK_OPENID_SUB_URI = "/protocol/openid-connect/";

    private static final String KEYCLOAK_AUTH_URL;
    private static final String KEYCLOAK_TOKEN_URL;
    private static final String KEYCLOAK_USERINFO_URL;
    private static final String KEYCLOAK_LOGOUT_URL;

    static {
        SERVICE_DOMAIN = System.getProperty("service.domain");
        KEYCLOAK_DOMAIN = System.getProperty("keycloak.domain") + "/realms/";
        KEYCLOAK_REALM = System.getProperty("keycloak.realm");
        KEYCLOAK_CLIENT_ID = System.getProperty("keycloak.client.id");
        KEYCLOAK_CLIENT_SECRET = System.getProperty("keycloak.client.secret");

        KEYCLOAK_AUTH_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "auth";
        KEYCLOAK_TOKEN_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "token";
        KEYCLOAK_USERINFO_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "userinfo";
        KEYCLOAK_LOGOUT_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "logout";
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        disableSslVerification();
        SpringApplication.run(Launcher.class);
    }

    @RequestMapping("")
    public String mainPage(HttpServletRequest request, HttpSession session, Model model) throws UnsupportedEncodingException {
        String redirectUri = ("/" + request.getContextPath() + "/login").replaceAll("//", "/");

        String state = Double.valueOf(new Random().nextDouble()).toString();
        session.setAttribute("state", state);
        model.addAttribute("state", state);
        model.addAttribute("clientId", KEYCLOAK_CLIENT_ID);
        model.addAttribute("redirectUri", URLEncoder.encode(SERVICE_DOMAIN + redirectUri, "utf-8"));
        model.addAttribute("KEYCLOAK_AUTH_URL", KEYCLOAK_AUTH_URL);
        return "main";
    }

    @RequestMapping("login")
    public String loginPage(HttpSession session, Model model, @ModelAttribute("code") String code, @ModelAttribute("state") String state) throws IOException {
        KeycloakToken token = getToken(code);
        final String accessToken = token.getAccessToken();
        session.setAttribute("accessToken", accessToken);
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("sessionState", session.getAttribute("state"));
        model.addAttribute("userInfo", getUserInfo(accessToken));
        model.addAttribute("token", token);

        return "user-info";
    }

    @RequestMapping("**")
    @ResponseBody
    public Map<String, String[]> anyPage(HttpServletRequest request) {
        return request.getParameterMap();
    }

    private KeycloakToken getToken(String code) throws IOException {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("grant_type", "authorization_code");
        parameters.set("client_id", KEYCLOAK_CLIENT_ID);
        parameters.set("client_secret", KEYCLOAK_CLIENT_SECRET);
        parameters.set("code", code);
        parameters.set("redirect_uri", SERVICE_DOMAIN + "/login");

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.postForObject(KEYCLOAK_TOKEN_URL, parameters, String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        KeycloakToken token = mapper.readValue(response, KeycloakToken.class);

        for (String s : token.getIdToken().split("[.]")) {
            try {
                String decoded = new String(Base64.getDecoder().decode(s));
                IdToken idToken = mapper.readValue(decoded, IdToken.class);
                token.setId(idToken);
            } catch (Exception ignored) {
            }
        }

        return token;
    }

    private Map getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        HttpEntity<Map> response = restTemplate.exchange(KEYCLOAK_USERINFO_URL, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}
