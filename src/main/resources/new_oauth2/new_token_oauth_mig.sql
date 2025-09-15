INSERT INTO oauth2_authorization (
    id,
    registered_client_id,
    principal_name,
    authorization_grant_type,
    authorized_scopes,
    access_token_issued_at,
    access_token_expires_at,
    refresh_token_issued_at,
    refresh_token_expires_at
)
SELECT
    gen_random_uuid(),
    (SELECT id FROM oauth2_registered_client WHERE client_id = oat.client_id LIMIT 1),
    oat.user_name,
    'authorization_code',              -- ajusta según tus flujos (puede ser password/client_credentials)
    oat.scope,
    NOW(),
    NOW() + INTERVAL '1 hour',         -- expiración ficticia de access_token
    NOW(),
    NOW() + INTERVAL '30 days'         -- expiración ficticia de refresh_token
FROM oauth_access_token oat
LEFT JOIN oauth_refresh_token ort ON oat.refresh_token = ort.token_id;
