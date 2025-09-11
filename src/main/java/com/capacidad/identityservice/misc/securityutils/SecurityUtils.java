package com.capacidad.identityservice.misc.securityutils;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.servlet.http.HttpServletRequest;


import org.apache.commons.lang3.StringUtils;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Optional<String> getBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        return StringUtils.isNotBlank(authorizationHeader) && StringUtils.startsWithIgnoreCase(authorizationHeader, "Bearer") ? Optional.of(JWTParser.removeJwtPrefix(authorizationHeader)) : Optional.empty();
    }

    public static Optional<String[]> getBasicAuthorizationCredentials(HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authorizationHeader) && StringUtils.startsWithIgnoreCase(authorizationHeader, "Basic")) {
                String token = (String)Stream.of(authorizationHeader).map((value) -> StringUtils.removeStartIgnoreCase(value, StringUtils.join(new String[]{"Basic", " "}))).map(String::trim).collect(Collectors.joining());
                Base64.Decoder dec = Base64.getDecoder();
                byte[] decodedToken = dec.decode(token);
                String decodedString = new String(decodedToken, StandardCharsets.UTF_8);
                String[] credentials = StringUtils.split(decodedString, ":", 2);
                if (credentials.length == 2) {
                    return Optional.of(credentials);
                }
            }

            return Optional.empty();
        } catch (IllegalArgumentException var7) {
            return Optional.empty();
        }
    }

    public static String getRequestIPAddress(HttpServletRequest request) {
        String ip = null;
        if (request != null) {
            String unknown = "unknown";
            ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.length() == 0 || StringUtils.equalsIgnoreCase(unknown, ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || StringUtils.equalsIgnoreCase(unknown, ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || StringUtils.equalsIgnoreCase(unknown, ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }

            if (ip == null || ip.length() == 0 || StringUtils.equalsIgnoreCase(unknown, ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }

            if (ip == null || ip.length() == 0 || StringUtils.equalsIgnoreCase(unknown, ip)) {
                ip = request.getRemoteAddr();
            }

            String[] splitted = StringUtils.split(ip, ",");
            if (splitted != null && splitted.length > 1) {
                ip = splitted[0];
            }
        }

        return ip;
    }
}
