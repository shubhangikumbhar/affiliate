/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.vocera.cloud.coremodel.constants.AffiliationStatus;
import com.vocera.cloud.coremodel.constants.HttpHeader;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.ErrorResponse;
import com.vocera.cloud.coremodel.model.Organization;
import com.vocera.cloud.coremodel.model.PageResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test cases for affiliation controller
 *
 * @author Rohit Phatak
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AffiliationControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private Gson gson = new Gson();

    /**
     * Initialize mockMvc.
     */
    @BeforeAll
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    /**
     * Test case for raising an affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void successfulAffiliationRequest() throws Exception {
        System.out.println("Executing test case for raising an affiliation request");

        Affiliation affiliationRequest = new Affiliation();

        Organization affiliationWith = new Organization();
        affiliationWith.setId(2);
        affiliationRequest.setAffiliationWith(affiliationWith);

        Organization affiliationFrom = new Organization();
        affiliationFrom.setId(3);
        affiliationRequest.setAffiliationFrom(affiliationFrom);

        affiliationRequest.setStatus(AffiliationStatus.ACTIVE_REQUEST);
        MvcResult response = mockMvc.perform(post("/affiliate").content(gson.toJson(affiliationRequest))
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(response);
    }

    /**
     * Test case for raising an affiliation request, while affiliation is already present in some form.
     *
     * @throws Exception
     */
    @Test
    public void affiliationAlreadyPresent() throws Exception {
        System.out.println("Executing test case for raising an affiliation request in case affiliation is already " +
                "present.");

        Affiliation affiliationRequest = new Affiliation();

        Organization affiliationWith = new Organization();
        affiliationWith.setId(1);
        affiliationRequest.setAffiliationWith(affiliationWith);

        Organization affiliationFrom = new Organization();
        affiliationFrom.setId(2);
        affiliationRequest.setAffiliationFrom(affiliationFrom);

        affiliationRequest.setStatus(AffiliationStatus.ACTIVE_REQUEST);
        MvcResult response = mockMvc.perform(post("/affiliate").content(gson.toJson(affiliationRequest))
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        JsonObject objResponse = gson.fromJson(response.getResponse().getContentAsString(), JsonObject.class);
        assertEquals("AFFILIATED", objResponse.get("status").getAsString());
    }

    /**
     * Invalid request for Affiliation.
     *
     * @throws Exception
     */
    @Test
    public void invalidAffiliationRequest() throws Exception {
        System.out.println("Executing test case for raising an affiliation request");

        Affiliation affiliationRequest = new Affiliation();

        affiliationRequest.setStatus(AffiliationStatus.ACTIVE_REQUEST);
        MvcResult response = mockMvc.perform(post("/affiliate").content(gson.toJson(affiliationRequest))
                .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andReturn();

        ErrorResponse errorResponse = gson.fromJson(response.getResponse().getContentAsString(), ErrorResponse.class);

        assertEquals(errorResponse.getMessage(), "Error Raising Affiliation Request");
    }

    /**
     * Filter through organizations within affiliation.
     *
     * @throws Exception
     */
    @Test
    public void defaultOrganizationFilter() throws Exception {
        System.out.println("Test case for default filter on organizations within affiliation");

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/organization/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&query=");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 2l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());
    }

    /**
     * Test case for filtering through affiliations
     *
     * @throws Exception
     */
    @Test
    public void defaultAffiliationFilter() throws Exception {
        System.out.println("Test case for default filter on affiliations");

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&query=");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 2l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());
    }

    /**
     * Filter through active affiliations.
     *
     * @throws Exception
     */
    @Test
    public void filterActiveAffiliations() throws Exception {
        System.out.println("Test case for filtering through active_affiliations");

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=ACTIVE_REQUESTS");

        MvcResult response = mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 2l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andReturn();

        gson = new Gson();
        Type apiResultType = new TypeToken<PageResponse<Affiliation>>() {
        }.getType();
        PageResponse<Affiliation> responseObj = gson.fromJson(response.getResponse().getContentAsString(),
                apiResultType);
        for (Affiliation affiliation : responseObj.getData())
            assertEquals(affiliation.getStatus(), AffiliationStatus.ACTIVE_REQUEST);

    }

    /**
     * Test case for fitlering affiliations which are affiliates.
     *
     * @throws Exception
     */
    @Test
    public void filterAffiliations() throws Exception {
        System.out.println("Test case for filtering through_affiliations with AFFILIATE status(Those organizations " +
                "which are affiliated).");

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=AFFILIATES");

        MvcResult response = mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 2l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andReturn();

        gson = new Gson();
        Type apiResultType = new TypeToken<PageResponse<Affiliation>>() {
        }.getType();
        PageResponse<Affiliation> responseObj = gson.fromJson(response.getResponse().getContentAsString(),
                apiResultType);
        for (Affiliation affiliation : responseObj.getData())
            assertEquals(affiliation.getStatus(), AffiliationStatus.AFFILIATED);

    }

    /**
     * Filter through organizations which have an affiliation in affiliation request state.
     *
     * @throws Exception
     */
    @Test
    public void filterActiveAffiliationOrganizations() throws Exception {
        System.out.println("Test case for filtering through active_affiliations");

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/organization/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=ACTIVE_REQUESTS");

        MvcResult response = mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 2l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andReturn();

    }
}