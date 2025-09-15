INSERT INTO oauth2_authorization_consent (
    registered_client_id,
    principal_name,
    authorities
)
SELECT
    (SELECT id FROM oauth2_registered_client WHERE client_id = oa.clientid LIMIT 1),
    oa.userid,
    oa.scope
FROM oauth_approvals oa;
