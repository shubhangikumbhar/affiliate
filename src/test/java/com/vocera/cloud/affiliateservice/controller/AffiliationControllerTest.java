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
import static org.junit.jupiter.api.Assertions.assertTrue;
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

        raiseAffiliationRequest(3l, 2l);
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

    /**
     * Test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully approving an affiliation request.");

        raiseAffiliationRequest(4l, 1l);
        actionAffiliation("/affiliate/approve/4", 1l, AffiliationStatus.AFFILIATED);
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        actionAffiliationError("/affiliate/approve/5", 1l, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for same organization should not be able to approve affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestFailureSameOrg() throws Exception {
        System.out.println("Failure test case for same organization should not be able to approve affiliation request");

        raiseAffiliationRequest(6l, 1l);
        actionAffiliationError("/affiliate/approve/1", 6l, AffiliationStatus.ACTIVE_REQUEST);
    }

    /**
     * Test case for successfully Rejecting an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully rejecting an affiliation request.");

        raiseAffiliationRequest(4l, 2l);
        actionAffiliation("/affiliate/reject/4", 2l, AffiliationStatus.REJECTED);
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        actionAffiliationError("/affiliate/reject/5", 2l, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for same organization should not be able to reject affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestFailureSameOrg() throws Exception {
        System.out.println("Failure test case for same organization should not be able to reject affiliation request.");

        raiseAffiliationRequest(6l, 2l);
        actionAffiliationError("/affiliate/approve/2", 6l, AffiliationStatus.ACTIVE_REQUEST);
    }

    /**
     * Test case for successfully Cancelling an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully cancelling an affiliation request.");

        raiseAffiliationRequest(4l, 3l);
        actionAffiliation("/affiliate/cancel/3", 4l, AffiliationStatus.CANCELLED);
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        actionAffiliationError("/affiliate/cancel/5", 3l, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for other organization should not be able to cancel affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestFailureOtherOrg() throws Exception {
        System.out.println("Failure test case for other organization should not be able to cancel affiliation request");

        raiseAffiliationRequest(6l, 3l);

        actionAffiliationError("/affiliate/approve/3", 6l, AffiliationStatus.ACTIVE_REQUEST);
    }

    public void raiseAffiliationRequest(Long affiliationFromId, Long affiliationWithId) throws Exception {
        Affiliation affiliationRequest = new Affiliation();

        Organization affiliationWith = new Organization();
        affiliationWith.setId(affiliationWithId);
        affiliationRequest.setAffiliationWith(affiliationWith);

        Organization affiliationFrom = new Organization();
        affiliationFrom.setId(affiliationFromId);
        affiliationRequest.setAffiliationFrom(affiliationFrom);

        affiliationRequest.setStatus(AffiliationStatus.ACTIVE_REQUEST);
        MvcResult affiliationRequestMockResponse =
                mockMvc.perform(post("/affiliate").content(gson.toJson(affiliationRequest))
                        .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andReturn();

        Affiliation affiliation = gson.fromJson(affiliationRequestMockResponse.getResponse().getContentAsString(),
                Affiliation.class);

        assertEquals(AffiliationStatus.ACTIVE_REQUEST, affiliation.getStatus());

        System.out.println("Affiliation Request raised successfully.");
    }

    public Affiliation actionAffiliation(
            String url, Long orgHeader, AffiliationStatus expectedStatus) throws Exception {
        MvcResult response = mockMvc.perform(post(url)
                .header(HttpHeader.ORGANIZATION_ID, orgHeader))
                .andExpect(status().isOk())
                .andReturn();

        Affiliation affiliation = gson.fromJson(response.getResponse().getContentAsString(),
                Affiliation.class);

        assertEquals(expectedStatus, affiliation.getStatus());

        return affiliation;
    }

    public void actionAffiliationError(String url, Long orgHeader, AffiliationStatus expectedStatus) throws Exception {
        MvcResult response = mockMvc.perform(post(url)
                .header(HttpHeader.ORGANIZATION_ID, orgHeader))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    /**
     * Test case for affiliation being revoked from the organization which has requested for affiliation.
     *
     * @throws Exception
     */
    @Test
    public void testRevokeAffiliationFrom() throws Exception {
        System.out.println("Test case for affiliation being revoked from the organization which has requested for " +
                "affiliation.");
        this.raiseAffiliationRequest(10l, 18l);
        this.actionAffiliation("/affiliate/approve/10", 18l, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/18", 10l, AffiliationStatus.REVOKED_FROM);
    }

    /**
     * Test case for affiliation being revoked from the organization which has been requested for affiliation.
     *
     * @throws Exception
     */
    @Test
    public void testRevokeAffiliationTo() throws Exception {
        System.out.println("Test case for affiliation being revoked from the organization which has been requested " +
                "for affiliation.");
        this.raiseAffiliationRequest(10l, 19l);
        this.actionAffiliation("/affiliate/approve/10", 19l, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/10", 19l, AffiliationStatus.REVOKED_TO);
    }

    /**
     * Failure test case for affiliation being revoked from the organization which has been requested for affiliation.
     *
     * @throws Exception
     */
    @Test
    public void testRevokeAffiliationToFailure() throws Exception {
        System.out.println("Failure test case for affiliation being revoked from the organization which has been " +
                "requested for affiliation.");

        mockMvc.perform(post("/affiliate/revoke/11")
                .header(HttpHeader.ORGANIZATION_ID, 19l))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Two organizations are already affiliated.
     * The organization which has requested affiliation, has revoked the affiliation.
     * In this case, the organization which has revoked the organization, should see the other organization in
     * unaffiliated organizations.
     * In the following test case organization with id 10 (affiliated_from) is affiliated with id 20 (affiliated_with).
     *
     * @throws Exception
     */
    @Test
    public void testRevokeFrom() throws Exception {
        System.out.println("Test case for checking if organization which is revoked is visible in Unaffiliation " +
                "organizations for organization which has revoked the affiliation");
        this.raiseAffiliationRequest(10l, 20l);
        this.actionAffiliation("/affiliate/approve/10", 20l, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/20", 10l, AffiliationStatus.REVOKED_FROM);

        // Check if organization with id 20 is visible in unaffiliated list for organization 10

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=UNAFFILIATED");
        filterURIBuilder.append("&query=LKQ");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 10l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());

        filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=ACTIVE_REQUESTS");
        filterURIBuilder.append("&query=Ball");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 20l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());
    }

    /**
     * Two organizations are already affiliated.
     * The organization which has approved affiliation, has revoked the affiliation.
     * In this case, the organization which has revoked the organization, should see the other organization in
     * unaffiliated organizations.
     * In the following test case organization with id 10 (affiliated_from) is affiliated with id 21 (affiliated_with).
     *
     * @throws Exception
     */
    @Test
    public void testRevokeWith() throws Exception {
        System.out.println("Test case for checking if organization which is revoked is visible in Unaffiliation " +
                "organizations for organization which has revoked the affiliation");
        this.raiseAffiliationRequest(10l, 21l);
        this.actionAffiliation("/affiliate/approve/10", 21l, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/10", 21l, AffiliationStatus.REVOKED_TO);

        // Check if organization with id 21 is visible in unaffiliated list for organization 10

        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=UNAFFILIATED");
        filterURIBuilder.append("&query=Ball");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 21l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());

        filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=ACTIVE_REQUESTS");
        filterURIBuilder.append("&query=J.M. Smucker");

        mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, 10l))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath());
    }

    /**
     * Test case for checking Ascending order for organization name.
     *
     * @throws Exception
     */
    @Test
    public void orderByName() throws Exception {
        System.out.println("Executing test case for checking ascending order on organization name.");
        StringBuilder filterURIBuilder = new StringBuilder();
        filterURIBuilder.append("/affiliate/filter?");
        filterURIBuilder.append("page=0");
        filterURIBuilder.append("&offset=1");
        filterURIBuilder.append("&filterType=UNAFFILIATED");

        final long organizationId = 10l;

        MvcResult response = mockMvc.perform(get(filterURIBuilder.toString())
                .header(HttpHeader.ORGANIZATION_ID, organizationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath()).andReturn();

        Type apiResultType = new TypeToken<PageResponse<Affiliation>>() {
        }.getType();
        PageResponse<Affiliation> affiliationPageResponse = gson.fromJson(response.getResponse().getContentAsString(),
                apiResultType);
        String previous = "";
        for (Affiliation affiliation : affiliationPageResponse.getData()) {
            if (!"".equals(previous)) {
                if (affiliation.getAffiliationFrom().getId() == organizationId) {
                    assertTrue(affiliation.getAffiliationWith().getName().compareTo(previous) < 0);
                    previous = affiliation.getAffiliationWith().getName();
                } else if (affiliation.getAffiliationWith().getId() == organizationId) {
                    assertTrue(affiliation.getAffiliationFrom().getName().compareTo(previous) < 0);
                    previous = affiliation.getAffiliationFrom().getName();
                } else {
                    // Fail test case since filter is not working properly
                    assertTrue(false);
                }
            } else {
                if (affiliation.getAffiliationFrom().getId() == organizationId) {
                    previous = affiliation.getAffiliationWith().getName();
                } else if (affiliation.getAffiliationWith().getId() == organizationId) {
                    previous = affiliation.getAffiliationFrom().getName();
                } else {
                    // Fail test case since filter is not working properly
                    assertTrue(false);
                }
            }
        }
    }
}
