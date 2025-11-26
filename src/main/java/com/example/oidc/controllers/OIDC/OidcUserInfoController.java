package com.example.oidc.controllers.OIDC;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.oidc.storage.UserInfo;
import com.example.oidc.storage.IOidcSessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OidcUserInfoController {
    private final IOidcSessionStore oidcSessionStore;

    @Autowired
    public OidcUserInfoController(IOidcSessionStore oidcSessionStore) {
        this.oidcSessionStore = oidcSessionStore;
    }

    @GetMapping("/userinfo")
    public Map<String, Object> userinfo(@RequestParam(value = "access_token", required = false) String accessToken,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> userInfo = new HashMap<>();
        if ((accessToken == null || accessToken.isEmpty()) && authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring("Bearer ".length());
        }
        if (accessToken == null || accessToken.isEmpty()) {
            userInfo.put("error", "Missing access_token");
            return userInfo;
        }
        UserInfo user = oidcSessionStore.getUserByToken(accessToken);
        if (user == null) {
            userInfo.put("error", "Invalid or expired access_token");
            return userInfo;
        }
        userInfo.put("sub", user.getSub());
        userInfo.put("givenname", user.getGivenName());
        userInfo.put("surname", user.getSurname());

        userInfo.put("country", user.getCountry());
        if (user.getDateOfBirth() != null) {
            userInfo.put("date_of_birth", user.getDateOfBirth().toString());
        }
        userInfo.put("phone_number", user.getPhoneNumber());
        userInfo.put("cert", user.getCert());
        return userInfo;
    }
}
