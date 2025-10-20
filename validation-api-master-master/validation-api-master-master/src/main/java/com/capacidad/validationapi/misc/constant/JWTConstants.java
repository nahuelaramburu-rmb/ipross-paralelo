package com.capacidad.validationapi.misc.constant;

public final class JWTConstants {

    public static final String JWT_PAYLOAD_USERNAME = "cognito:username";
    public static final String JWT_PAYLOAD_GROUPS = "cognito:groups";
    public static final String JWT_PAYLOAD_PERMISSIONS = "permissions";
    public static final String JWT_HEADER_KEYS_ID = "kid";
    public static final String JWT_PAYLOAD_SUBJECT = "sub";
    public static final String JWT_PAYLOAD_RESOURCE_ID = "custom:resource_id";

    private JWTConstants() {

    }

}
