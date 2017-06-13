package org.tinywind.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tinywind.model.KeycloakToken;
import org.tinywind.service.KeycloakService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;

import static org.tinywind.service.KeycloakService.*;

/**
 * @author tinywind
 */
@Controller
public class MainController {

    private static final Log log = LogFactory.getLog(MainController.class);

    @Autowired
    private KeycloakService service;

    @RequestMapping("")
    public String mainPage(HttpServletRequest request, HttpSession session, Model model) throws UnsupportedEncodingException {
        String redirectUri = ("/" + request.getContextPath() + "/login").replaceAll("//", "/");

        String state = Double.valueOf(new Random().nextDouble()).toString();
        session.setAttribute("state", state);
        model.addAttribute("state", state);
        model.addAttribute("clientId", KEYCLOAK_CLIENT_ID);
        model.addAttribute("redirectUri", URLEncoder.encode(SERVICE_DOMAIN + redirectUri, "utf-8"));
        model.addAttribute("authUrl", KEYCLOAK_AUTH_URL);
        return "main";
    }

    @RequestMapping("login")
    public String loginPage(HttpSession session, Model model, @ModelAttribute("code") String code, @ModelAttribute("state") String state) throws IOException {
        KeycloakToken token = service.getToken(code);
        final String accessToken = token.getAccessToken();
        session.setAttribute("accessToken", accessToken);
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("sessionState", session.getAttribute("state"));
        model.addAttribute("userInfo", service.getUserInfo(accessToken));
        model.addAttribute("token", token);

        return "user-info";
    }

    @RequestMapping("user-info")
    public String userInfoPage(Model model, String token) throws IOException {
        model.addAttribute("userInfo", service.getUserInfo(token));

        return "user-info";
    }

    @RequestMapping("**")
    @ResponseBody
    public Map<String, String[]> anyPage(HttpServletRequest request) {
        return request.getParameterMap();
    }
}
