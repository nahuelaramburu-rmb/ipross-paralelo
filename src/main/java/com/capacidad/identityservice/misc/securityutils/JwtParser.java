package com.capacidad.identityservice.misc.securityutils;


import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;


final class JwtParser {
    private static final int HEADER = 0;
    private static final int PAYLOAD = 1;
    private static final int JWT_PARTS = 3;
    private static final String INVALID_ERROR_MESSAGE = "Invalid JWT";
    private static final String PARSING_ERROR_MESSAGE = "Error parsing JWT";


    private JwtParser() {
    }

    static JSONObject getHeader(String jwt) {
        try {
            String parsedJwt = removeJwtPrefix(jwt);
            validateJWT(parsedJwt);
            Base64.Decoder dec = Base64.getDecoder();
            byte[] sectionDecoded = dec.decode(parsedJwt.split("\\.")[0]);
            String jwtSection = new String(sectionDecoded, StandardCharsets.UTF_8);
            return new JSONObject(jwtSection);
        } catch (Exception var5) {
            throw new InvalidParameterException("Error parsing JWT");
        }
    }

    private static JSONObject getPayload(String jwt) {
        try {
            validateJWT(jwt);
            Base64.Decoder dec = Base64.getDecoder();
            String payload = jwt.split("\\.")[1];
            byte[] sectionDecoded = dec.decode(payload);
            String jwtSection = new String(sectionDecoded, StandardCharsets.UTF_8);
            return new JSONObject(jwtSection);
        } catch (Exception var5) {
            throw new InvalidParameterException("Error parsing JWT");
        }
    }

    static String getClaim(String jwt, String claim) {
        try {
            String parsedJwt = removeJwtPrefix(jwt);
            JSONObject payload = getPayload(parsedJwt);
            Object claimValue = payload.keySet().contains(claim) ? payload.get(claim) : null;
            return claimValue != null ? claimValue.toString() : "";
        } catch (Exception var5) {
            throw new InvalidParameterException("Invalid JWT");
        }
    }

    static boolean checkClaim(String jwt, String claim) {
        try {
            String parsedJwt = removeJwtPrefix(jwt);
            JSONObject payload = getPayload(parsedJwt);
            return payload.keySet().contains(claim);
        } catch (Exception var4) {
            throw new InvalidParameterException("Invalid JWT");
        }
    }

    static String[] getClaimAsArray(String jwt, String claim) {
        try {
            String parsedJwt = removeJwtPrefix(jwt);
            JSONObject payload = getPayload(parsedJwt);
            JSONArray claimValue = (JSONArray)payload.get(claim);
            String[] results = new String[claimValue.length()];

            for(int i = 0; i < results.length; ++i) {
                results[i] = claimValue.optString(i);
            }

            if (results.length > 0) {
                return results;
            }
        } catch (Exception var7) {
            throw new InvalidParameterException("Invalid JWT");
        }

        return new String[0];
    }

    static String removeJwtPrefix(String authorizationHeader) {
        return (String)Optional.ofNullable(authorizationHeader).map((value) -> StringUtils.removeStartIgnoreCase(value, StringUtils.join(new String[]{"Bearer", " "}))).orElseThrow(() -> new InvalidParameterException("Invalid JWT"));
    }

    private static void validateJWT(String jwt) {
        String[] jwtParts = jwt.split("\\.");
        if (jwtParts.length != 3) {
            throw new InvalidParameterException("Malformed JWT");
        }
    }



}

