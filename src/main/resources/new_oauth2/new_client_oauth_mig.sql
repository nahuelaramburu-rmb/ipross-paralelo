INSERT INTO oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    scopes,
    client_settings,
    token_settings
)
SELECT
    gen_random_uuid(),                       -- nuevo ID único (Postgres). En MySQL usar UUID() o generar manualmente.
    client_id,
    NOW(),
    client_secret,
    client_id,                               -- usamos client_id como nombre (puedes personalizarlo)
    'client_secret_basic',                   -- método típico de auth
    authorized_grant_types,
    web_server_redirect_uri,
    scope,
    '{"requireProofKey":false,"requireAuthorizationConsent":false}', -- JSON con settings mínimos
    '{"accessTokenTimeToLive":"PT1H","refreshTokenTimeToLive":"P30D"}' -- JSON con settings mínimos
FROM oauth_client_details;
