package com.btctaxi.gate.controller;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import genesis.gate.config.DistConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
public class ZendeskController extends BaseController {
    private static final String SHARED_KEY = "mdkGP2vadFUqWBFa7nRVrrfQkdS7QV5uHoZodknFt9dvl2ls";

    @Autowired
    private DistConfig distConfig;

    @RequestMapping("/gate/zendesk/auth")
    public void auth(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        JWTClaimsSet jwtClaims = new JWTClaimsSet();
        jwtClaims.setIssuedAtClaim((int) (System.currentTimeMillis() / 1000));
        jwtClaims.setJWTIDClaim(UUID.randomUUID().toString());
        jwtClaims.setCustomClaim("name", sess.getNick());
        jwtClaims.setCustomClaim("email", sess.getEmail());

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setContentType("text/plain");

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaims.toJSONObject()));

        JWSSigner signer = new MACSigner(SHARED_KEY.getBytes());

        try {
            jwsObject.sign(signer);
        } catch (com.nimbusds.jose.JOSEException e) {
            System.err.println("Error signing JWT: " + e.getMessage());
            return;
        }

        String jwtString = jwsObject.serialize();

        String redirectUrl = "https://" + distConfig.getZendeskPrefix() + ".zendesk.com/access/jwt?jwt=" + jwtString;

        String returnTo = request.getParameter("return_to");
        if (returnTo != null) {
            redirectUrl += "&return_to=" + encode(returnTo);
        } else {
            String lang = sess.getLocale();
            if (lang.equals("zh-CN")) {
                returnTo = "https://" + distConfig.getZendeskPrefix() + ".zendesk.com/hc/zh-cn";
            } else {
                returnTo = "https://" + distConfig.getZendeskPrefix() + ".zendesk.com/hc/en-us";
            }

            redirectUrl += "&return_to=" + encode(returnTo);
        }

        response.sendRedirect(redirectUrl);
    }

    private static String encode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            System.err.println("UTF-8 is not supported!");
            return url;
        }
    }
}
