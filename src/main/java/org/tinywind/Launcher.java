package org.tinywind;

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
import java.util.Map;
import java.util.Random;

/**
 * @author tinywind
 */
@Controller
@SpringBootApplication
public class Launcher {

    private static final Log log = LogFactory.getLog(Launcher.class);
    private static final String MYSERVICE_DOMAIN = "http://localhost:8000";
    private static final String KEYCLOAK_DOMAIN = "https://211.253.10.111:38443/auth/realms/";
    private static final String KEYCLOAK_REALM = "apiserver";
    private static final String KEYCLOAK_CLIENT_ID = "local";
    private static final String KEYCLOAK_CLIENT_SECRET = "62327148-31f2-4680-8422-d7f0a94fc629";

    private static final String KEYCLOAK_OPENID_SUB_URI = "/protocol/openid-connect/";

    private static final String KEYCLOAK_AUTH_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "auth";
    private static final String KEYCLOAK_TOKEN_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "token";
    private static final String KEYCLOAK_USERINFO_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "userinfo";
    private static final String KEYCLOAK_LOGOUT_URL = KEYCLOAK_DOMAIN + KEYCLOAK_REALM + KEYCLOAK_OPENID_SUB_URI + "logout";

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
        model.addAttribute("redirectUri", URLEncoder.encode(MYSERVICE_DOMAIN + redirectUri, "utf-8"));
        model.addAttribute("KEYCLOAK_AUTH_URL", KEYCLOAK_AUTH_URL);
        return "main";
    }

    @RequestMapping("login")
    public String loginPage(HttpSession session, Model model, @ModelAttribute("code") String code, @ModelAttribute("state") String state) throws IOException {
        final String accessToken = getAccessToken(code);
        session.setAttribute("accessToken", accessToken);
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("sessionState", session.getAttribute("state"));
        model.addAttribute("userInfo", getUserInfo(accessToken));

        return "user-info";
    }

    private String getAccessToken(String code) {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("grant_type", "authorization_code");
        parameters.set("client_id", KEYCLOAK_CLIENT_ID);
        parameters.set("client_secret", KEYCLOAK_CLIENT_SECRET);
        parameters.set("code", code);
        parameters.set("redirect_uri", MYSERVICE_DOMAIN + "/login");

        RestTemplate restTemplate = new RestTemplate();
        Map response = restTemplate.postForObject(KEYCLOAK_TOKEN_URL, parameters, Map.class);
        Object accessToken = response.get("access_token");

        return accessToken.toString();
    }

    private Map getUserInfo(String accessToken) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        HttpEntity<Map> response = restTemplate.exchange(KEYCLOAK_USERINFO_URL, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}
