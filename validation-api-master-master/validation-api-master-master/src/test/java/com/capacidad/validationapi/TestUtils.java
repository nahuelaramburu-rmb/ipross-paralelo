package com.capacidad.validationapi;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.MultiValueMap;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.AUTHORIZATION_BEARER_PREFIX;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.HEADER_AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
public final class TestUtils {

    private TestUtils() {
    }

    public static void truncateDatabaseTables(ApplicationContext applicationContext,
                                              String... tableNames) throws SQLException {
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        String resetSqlTemplate = "TRUNCATE TABLE %s RESTART IDENTITY CASCADE";
        String resetSql = "";
        try (Connection dbConnection = dataSource.getConnection()) {
            //Create SQL statements that reset the auto increment columns and invoke
            //the created SQL statements.
            for (String resetSqlArgument : tableNames) {
                try (Statement statement = dbConnection.createStatement()) {
                    resetSql = String.format(resetSqlTemplate, resetSqlArgument);
                    statement.execute(resetSql);
                } catch (Exception ex) {
                    log.debug("Truncate Error: {}, Query: {}", ex.getMessage(), resetSql);
                }
            }
        }
    }

    public static JSONObject createAndGetObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult postResult = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isCreated())
                .andReturn();
        String location = postResult.getResponse().getHeaders(IntegrationTestConstants.LOCATION_HEADER).get(0);
        return getAndReturnObject(mockMvc, location, token, "");
    }

    public static JSONObject createFormAndGetObject(MockMvc mockMvc, String endpoint, String token, MultiValueMap<String, String> params) throws Exception {
        MvcResult postResult = mockMvc.perform(multipart(endpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .params(params))
                .andExpect(status().isCreated())
                .andReturn();
        String location = postResult.getResponse().getHeaders(IntegrationTestConstants.LOCATION_HEADER).get(0);
        return getAndReturnObject(mockMvc, location, token, "");
    }

    public static JSONObject postAndGetObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult postResult = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
        String contentResponse = postResult.getResponse().getContentAsString();
        return new JSONObject(contentResponse);
    }

    public static MockHttpServletResponse postAndGetResponse(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        return mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
    }

    public static JSONObject putAndReturnObject(MockMvc mockMvc, String endpoint, String token) throws Exception {
        MvcResult putResult = mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk())
                .andReturn();
        String contentResponse = putResult.getResponse().getContentAsString();
        return new JSONObject(contentResponse);
    }

    public static JSONObject putAndReturnObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult putResult = mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
        String contentResponse = putResult.getResponse().getContentAsString();
        return new JSONObject(contentResponse);
    }

    public static long createAndReturnLocationId(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult postResult = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isCreated())
                .andReturn();
        String location = postResult.getResponse().getHeaders(IntegrationTestConstants.LOCATION_HEADER).get(0);
        String[] splittedLocation = StringUtils.split(location, SLASH);
        return Long.parseLong(splittedLocation[splittedLocation.length - 1]);
    }

    public static JSONObject patchObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult patchResult = mockMvc.perform(patch(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONObject(patchResult.getResponse().getContentAsString());
    }

    public static void createObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isCreated());
    }

    public static JSONObject createObjectAndGetResult(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult postResult = mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONObject(postResult.getResponse().getContentAsString());
    }

    public static void deleteObject(MockMvc mockMvc, String endpoint, String token) throws Exception {
        mockMvc.perform(delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk());
    }

    public static JSONObject deleteAndReturnObject(MockMvc mockMvc, String endpoint, String token) throws Exception {
        MvcResult putResult = mockMvc.perform(delete(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk())
                .andReturn();
        String contentResponse = putResult.getResponse().getContentAsString();
        return new JSONObject(contentResponse);
    }

    public static void createObject(MockMvc mockMvc, String endpoint, String content, ResultMatcher expectedResult, String token) throws Exception {
        mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(expectedResult);
    }

    public static JSONObject getAndReturnObject(MockMvc mockMvc, String endpoint, String token, Object... uriVars) throws Exception {
        MvcResult getResult = mockMvc.perform(get(endpoint, uriVars)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONObject(getResult.getResponse().getContentAsString());
    }

    public static JSONArray getAndReturnArray(MockMvc mockMvc, String endpoint, String token) throws Exception {
        MvcResult getResult = mockMvc.perform(get(endpoint)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONArray(getResult.getResponse().getContentAsString());
    }

    public static JSONObject putObject(MockMvc mockMvc, String endpoint, String content, String token) throws Exception {
        MvcResult putResult = mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token))
                .content(content))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONObject(putResult.getResponse().getContentAsString());
    }

    public static void putObject(MockMvc mockMvc, String endpoint, String token) throws Exception {
        mockMvc.perform(put(endpoint)
                .header(HEADER_AUTHORIZATION, StringUtils.join(AUTHORIZATION_BEARER_PREFIX, token)))
                .andExpect(status().isOk());
    }

}