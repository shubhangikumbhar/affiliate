/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vocera.cloud.coremodel.constants.AffiliationStatus;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.ErrorResponse;
import com.vocera.cloud.coremodel.model.Organization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
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
@TestMethodOrder(OrderAnnotation.class)
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
}