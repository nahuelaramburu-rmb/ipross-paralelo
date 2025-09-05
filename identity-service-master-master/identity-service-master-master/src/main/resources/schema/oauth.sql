CREATE TABLE IF NOT EXISTS oauth_client_details
(
    client_id               VARCHAR(255) PRIMARY KEY,
    resource_ids            VARCHAR(255),
    client_secret           VARCHAR(255),
    scope                   VARCHAR(255),
    authorized_grant_types  VARCHAR(255),
    web_server_redirect_uri VARCHAR(255),
    authorities             VARCHAR(255),
    access_token_validity   INTEGER,
    refresh_token_validity  INTEGER,
    additional_information  VARCHAR(4096),
    autoapprove             VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS oauth_client_token
(
    token_id          VARCHAR(255),
    token             BYTEA,
    authentication_id VARCHAR(255) PRIMARY KEY,
    user_name         VARCHAR(255),
    client_id         VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS oauth_access_token
(
    token_id          VARCHAR(255),
    token             BYTEA,
    authentication_id VARCHAR(255) PRIMARY KEY,
    user_name         VARCHAR(255),
    client_id         VARCHAR(255),
    authentication    BYTEA,
    refresh_token     VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS oauth_refresh_token
(
    token_id       VARCHAR(255),
    token          BYTEA,
    authentication BYTEA
);


CREATE TABLE IF NOT EXISTS oauth_code
(
    code           VARCHAR(255),
    authentication BYTEA
);


CREATE TABLE IF NOT EXISTS oauth_approvals
(
    userId         VARCHAR(255),
    clientId       VARCHAR(255),
    scope          VARCHAR(255),
    status         VARCHAR(10),
    expiresAt      TIMESTAMP,
    lastModifiedAt TIMESTAMP
);


CREATE TABLE IF NOT EXISTS clientdetails
(
    appId                  VARCHAR(255) PRIMARY KEY,
    resourceIds            VARCHAR(255),
    appSecret              VARCHAR(255),
    scope                  VARCHAR(255),
    grantTypes             VARCHAR(255),
    redirectUrl            VARCHAR(255),
    authorities            VARCHAR(255),
    access_token_validity  INTEGER,
    refresh_token_validity INTEGER,
    additionalInformation  VARCHAR(4096),
    autoApproveScopes      VARCHAR(255)
);

-- Workaround trigger for oauth2 duplicate key oauth_access_token bug

CREATE OR REPLACE FUNCTION append_suffix_oauth_access_token()
  RETURNS TRIGGER
  LANGUAGE PLPGSQL
  AS
$$
	declare suffix varchar(50) := CONCAT(extract(epoch from now()),SUBSTRING(MD5(random()::text) FROM 1 FOR 8));
	begin
        IF (exists(SELECT 1 FROM oauth_access_token WHERE authentication_id = NEW.authentication_id)) THEN
             NEW.authentication_id := CONCAT(NEW.authentication_id, suffix);
        END IF;

        IF (exists(SELECT 1 FROM oauth_access_token WHERE token_id = NEW.token_id)) THEN
             NEW.token_id := CONCAT(NEW.token_id, suffix);
        END IF;
        return new;
    end
$$;

CREATE TRIGGER handle_duplicate_authentication_id

BEFORE INSERT ON oauth_access_token
FOR EACH ROW EXECUTE PROCEDURE append_suffix_oauth_access_token();

CREATE UNIQUE INDEX token_id_unique_idx ON oauth_client_token (token_id);