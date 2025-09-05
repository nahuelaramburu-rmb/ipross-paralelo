package com.capacidad.validationapi.config.security;

import com.capacidad.utils.SecurityUtils;
import com.capacidad.utils.exception.ExpiredTokenException;
import com.capacidad.utils.exception.InvalidTokenException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class IdentityClientService {

    private final ApplicationProperties applicationProperties;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private static final String VERIFY_TOKEN_ENDPOINT = "/oauth/verify_token";
    private static final String TOKEN_ENDPOINT = "/oauth/token";
    private static final String USERS_ENDPOINT = "/users";

    @Autowired
    public IdentityClientService(ApplicationProperties applicationProperties,
                                 RestTemplateBuilder restTemplateBuilder,
                                 ObjectMapper objectMapper) {
        this.applicationProperties = applicationProperties;
        this.restTemplateBuilder = restTemplateBuilder;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    protected void initRestTemplate() {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void verify(HttpServletRequest request) {
        String jwt = SecurityUtils.getBearerToken(request).orElse("");
        verify(jwt);
    }

    public void verify(String jwt) {
        try {
            String parsedJwt = StringUtils.removeIgnoreCase(jwt, "Bearer").trim();
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.put("token", Collections.singletonList(parsedJwt));
            String host = StringUtils.join(applicationProperties.getIdentityHost(), VERIFY_TOKEN_ENDPOINT);
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(host)
                    .queryParams(queryParams);
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(applicationProperties.getIdentityClientId(), applicationProperties.getIdentityClientSecret());
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            restTemplate.postForEntity(builder.build().toUri(), entity, String.class);
        } catch (HttpClientErrorException e) {
            JsonNode response = parseResponse(e.getResponseBodyAsString());
            String type = response.get("type").asText();
            String code = response.get("code").asText();
            if (type.equals("ExpiredToken"))
                throw new ExpiredTokenException(code);
            throw new InvalidTokenException(code);
        }
    }

    private JsonNode parseResponse(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "");
            node.put("code", "");
            return node;
        }
    }

    public Optional<String> exchangeClientCredentialsForJwt(String clientId, String clientSecret) {
        try {
            log.info("{} - exchangeBasicAuthForJwt", this.getClass());
            String host = StringUtils.join(applicationProperties.getIdentityHost(), TOKEN_ENDPOINT);
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(host);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(builder.build().toUri(), entity, String.class);
            JsonNode nodeResponse = objectMapper.readTree(response.getBody());
            return Optional.of(nodeResponse.get("access_token").asText());
        } catch (JsonProcessingException e) {
            log.error("{} - exchangeBasicAuthForJwt: {}", this.getClass(), e.getMessage());
        }
        return Optional.empty();
    }

    public void deleteResourceIdAccounts(UUID resourceId) throws ObjectNotValidException {
        try {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.put("resource_id", Collections.singletonList(resourceId.toString()));
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(StringUtils.join(applicationProperties.getIdentityHost(),
                    USERS_ENDPOINT))
                    .queryParams(queryParams);
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(applicationProperties.getIdentityClientId(), applicationProperties.getIdentityClientSecret());
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, String.class);
        } catch (Exception e) {
            log.error("{} - deleteResourceIdAccounts: {}", this.getClass(), e.getMessage());
            throw new ObjectNotValidException("identityClient.cannotDeleteUsers");
        }
    }

}
