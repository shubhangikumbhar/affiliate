/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.vocera.cloud.affiliateservice.constant.ErrorCodes;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

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
    private void setup() {
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

        raiseAffiliationRequest(3L, 2L);
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
        MvcResult response = mockMvc.perform(post("/affiliate")
                .header(HttpHeader.ORGANIZATION_ID, 2L)
                .content(gson.toJson(affiliationRequest))
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
        MvcResult response = mockMvc.perform(post("/affiliate")
                .header(HttpHeader.ORGANIZATION_ID, 1L)
                .content(gson.toJson(affiliationRequest))
                .contentType("application/json"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").hasJsonPath())
                .andReturn();

        ErrorResponse errorResponse = gson.fromJson(response.getResponse().getContentAsString(), ErrorResponse.class);

        assertEquals(ErrorCodes.INVALID_REQUEST.getCode(), errorResponse.getCode());
    }

    /**
     * Filter through organizations within affiliation.
     *
     * @throws Exception
     */
    @Test
    public void defaultOrganizationFilter() throws Exception {
        System.out.println("Test case for default filter on organizations within affiliation");

        this.filterExpectValues(2L, "/affiliate/organization/filter?", "page=0", "&offset=1", "&query=");
    }

    /**
     * Test case for filtering through affiliations
     *
     * @throws Exception
     */
    @Test
    public void defaultAffiliationFilter() throws Exception {
        System.out.println("Test case for default filter on affiliations");

        this.filterExpectValues(2L, "/affiliate/filter?", "page=0", "&offset=1", "&query=");
    }

    /**
     * Filter through active affiliations.
     *
     * @throws Exception
     */
    @Test
    public void filterActiveAffiliations() throws Exception {
        System.out.println("Test case for filtering through active_affiliations");

        MvcResult response = this.filterExpectValues(2L, "/affiliate/filter?", "page=0", "&offset=1", "&filterType" +
                "=ACTIVE_REQUESTS");

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

        MvcResult response = this.filterExpectValues(2L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=AFFILIATES");

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

        this.filterExpectValues(2L, "/affiliate/organization/filter?", "page=0", "&offset=1",
                "&filterType=ACTIVE_REQUESTS");

    }

    /**
     * Test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully approving an affiliation request.");

        raiseAffiliationRequest(4L, 1L);
        actionAffiliation("/affiliate/approve/4", 1L, AffiliationStatus.AFFILIATED);
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        actionAffiliationError("/affiliate/approve/5", 1L, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for same organization should not be able to approve affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void approveAffiliationRequestFailureSameOrg() throws Exception {
        System.out.println("Failure test case for same organization should not be able to approve affiliation request");

        raiseAffiliationRequest(6L, 1L);
        actionAffiliationError("/affiliate/approve/1", 6L, AffiliationStatus.ACTIVE_REQUEST);
    }

    /**
     * Test case for successfully Rejecting an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully rejecting an affiliation request.");

        raiseAffiliationRequest(4L, 2L);
        actionAffiliation("/affiliate/reject/4", 2L, AffiliationStatus.REJECTED);
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        actionAffiliationError("/affiliate/reject/5", 2L, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for same organization should not be able to reject affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void rejectAffiliationRequestFailureSameOrg() throws Exception {
        System.out.println("Failure test case for same organization should not be able to reject affiliation request.");

        raiseAffiliationRequest(6L, 2L);
        actionAffiliationError("/affiliate/approve/2", 6L, AffiliationStatus.ACTIVE_REQUEST);
    }

    /**
     * Test case for successfully Cancelling an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestSuccessful() throws Exception {
        System.out.println("Test case for successfully cancelling an affiliation request.");

        this.raiseAffiliationRequest(4L, 3L);
        this.actionAffiliation("/affiliate/cancel/3", 4L, AffiliationStatus.CANCELLED);

        this.filterExpectValues(4L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Maximilian Garner");
        this.filterExpectValues(3L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=ACTIVE_REQUESTS", "&query=Yuliana Colon");
    }

    /**
     * Failure test case for successfully Approving an Affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestFailure() throws Exception {
        System.out.println("Failure test case for successfully approving an affiliation request.");

        this.actionAffiliationError("/affiliate/cancel/5", 3L, AffiliationStatus.NONE);
    }

    /**
     * Failure test case for other organization should not be able to cancel affiliation request.
     *
     * @throws Exception
     */
    @Test
    public void cancelAffiliationRequestFailureOtherOrg() throws Exception {
        System.out.println("Failure test case for other organization should not be able to cancel affiliation request");

        this.raiseAffiliationRequest(6L, 3L);

        this.actionAffiliationError("/affiliate/approve/3", 6L, AffiliationStatus.ACTIVE_REQUEST);
    }

    private void raiseAffiliationRequest(Long affiliationFromId, Long affiliationWithId) throws Exception {
        Affiliation affiliationRequest = new Affiliation();

        Organization affiliationWith = new Organization();
        affiliationWith.setId(affiliationWithId);
        affiliationRequest.setAffiliationWith(affiliationWith);

        Organization affiliationFrom = new Organization();
        affiliationFrom.setId(affiliationFromId);
        affiliationRequest.setAffiliationFrom(affiliationFrom);

        affiliationRequest.setStatus(AffiliationStatus.ACTIVE_REQUEST);
        MvcResult affiliationRequestMockResponse =
                mockMvc.perform(post("/affiliate")
                        .header(HttpHeader.ORGANIZATION_ID, affiliationFromId)
                        .content(gson.toJson(affiliationRequest))
                        .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andReturn();

        Affiliation affiliation = gson.fromJson(affiliationRequestMockResponse.getResponse().getContentAsString(),
                Affiliation.class);

        assertEquals(AffiliationStatus.ACTIVE_REQUEST, affiliation.getStatus());

        System.out.println("Affiliation Request raised successfully.");
    }

    private Affiliation actionAffiliation(
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

    private void actionAffiliationError(String url, Long orgHeader, AffiliationStatus expectedStatus) throws Exception {
        MvcResult response = mockMvc.perform(post(url)
                .header(HttpHeader.ORGANIZATION_ID, orgHeader))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    private String filterURIBuilder(String... url) {
        return this.encodeValue(Arrays.stream(url).collect(Collectors.joining()));
    }

    private MvcResult filterExpectBlank(Long orgId, String... url) throws Exception {
        return mockMvc.perform(get(this.filterURIBuilder(url))
                .header(HttpHeader.ORGANIZATION_ID, orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }

    private MvcResult filterExpectValues(Long orgId, String... url) throws Exception {
        return mockMvc.perform(get(this.filterURIBuilder(url))
                .header(HttpHeader.ORGANIZATION_ID, orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andReturn();
    }

    private String encodeValue(String value) {
        try {
            URI uri = new URI(null, null, value.substring(0, value.indexOf("?")),
                    value.substring(value.indexOf("?") + 1), null);
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getCause());
        }
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
        this.raiseAffiliationRequest(10L, 18L);
        this.actionAffiliation("/affiliate/approve/10", 18L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/18", 10L, AffiliationStatus.REVOKED_FROM);
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
        this.raiseAffiliationRequest(10L, 19L);
        this.actionAffiliation("/affiliate/approve/10", 19L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/10", 19L, AffiliationStatus.REVOKED_TO);
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
                .header(HttpHeader.ORGANIZATION_ID, 19L))
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
        this.raiseAffiliationRequest(10L, 20L);
        this.actionAffiliation("/affiliate/approve/10", 20L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/20", 10L, AffiliationStatus.REVOKED_FROM);

        this.filterExpectValues(10L, "/affiliate/filter?", "page=0", "&offset=1", "&filterType=UNAFFILIATED",
                "&query=LKQ");

        this.filterExpectValues(20L, "/affiliate/filter?", "page=0", "&offset=1", "&filterType=ACTIVE_REQUESTS",
                "&query=Ball");
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
        this.raiseAffiliationRequest(10L, 21L);
        this.actionAffiliation("/affiliate/approve/10", 21L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/10", 21L, AffiliationStatus.REVOKED_TO);
        this.filterExpectValues(21L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Ball");
        this.filterExpectValues(10L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=ACTIVE_REQUESTS", "&query=J.M. Smucker");
    }

    /**
     * Test case for checking Ascending order for organization name.
     *
     * @throws Exception
     */
    @Test
    public void orderByName() throws Exception {
        System.out.println("Executing test case for checking ascending order on organization name.");

        final long organizationId = 10l;
        MvcResult response = this.filterExpectValues(organizationId, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED");

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

    /**
     * Affiliation request has been raised from OrganizationA to OrganizationB.
     * Then request is cancelled from OrganizationA.
     * Now OrganizationA can see OrganizarionB in Unaffiliated Organizations.
     * OrganizationA wants to resend affiliation request to OrganizationB.
     *
     * @throws Exception
     */
    @Test
    public void reraiseAffiliationCancelled() throws Exception {
        System.out.println("Reraise affiliation request after cancelled");

        this.raiseAffiliationRequest(15L, 16L);
        this.actionAffiliation("/affiliate/cancel/16", 15L, AffiliationStatus.CANCELLED);
        this.raiseAffiliationRequest(15L, 16L);
    }

    /**
     * Affiliation request has been raised from OrganizationA to OrganizationB.
     * Then request is Rejected from OrganizationB.
     * Now OrganizationA can see OrganizationB in Active Requests.
     * OrganizationB wants to resend affiliation request to OrganizationA.
     *
     * @throws Exception
     */
    @Test
    public void reraiseAffiliationRejected() throws Exception {
        System.out.println("Reraise affiliation request after cancelled");

        this.raiseAffiliationRequest(15L, 17L);
        this.actionAffiliation("/affiliate/reject/15", 17L, AffiliationStatus.REJECTED);
        this.raiseAffiliationRequest(17L, 15L);
    }

    /**
     * Affiliation request has been raised from OrganizationA to OrganizationB.
     * Then request is Accepted by OrganizationB and both organizations are Affiliated.
     * Now OrganizationA revokes affiliation with OrganizationB and OrganizationB is seen in Active Requests.
     * OrganizationB wants to resend affiliation request to OrganizationA.
     *
     * @throws Exception
     */
    @Test
    public void reraiseAffiliationRevokeFrom() throws Exception {
        System.out.println("Reraise affiliation request after cancelled");

        this.raiseAffiliationRequest(15L, 18L);
        this.actionAffiliation("/affiliate/approve/15", 18L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/18", 15L, AffiliationStatus.REVOKED_FROM);
        this.raiseAffiliationRequest(15L, 18L);
    }

    /**
     * Affiliation request has been raised from OrganizationA to OrganizationB.
     * Then request is Accepted by OrganizationB and both organizations are Affiliated.
     * Now OrganizationB revokes affiliation with OrganizationA and OrganizationA is seen in Active Requests.
     * OrganizationB wants to resend affiliation request to OrganizationA.
     *
     * @throws Exception
     */
    @Test
    public void reraiseAffiliationRevokeTo() throws Exception {
        System.out.println("Reraise affiliation request after cancelled");

        this.raiseAffiliationRequest(15L, 19L);
        this.actionAffiliation("/affiliate/approve/15", 19L, AffiliationStatus.AFFILIATED);
        this.actionAffiliation("/affiliate/revoke/15", 19L, AffiliationStatus.REVOKED_TO);
        this.raiseAffiliationRequest(19L, 15L);
    }

    /**
     * Affiliation Request is raised from OrganizationA to OrganizationB.
     * This request is cancelled from OrganizationA.
     * As a result of this OrganizationB can see this request in ActiveRequest section.
     * OrganizationB can now choose to remove the affiliation from the ActiveRequest section.
     * After removing the affiliation request, both organizations can see each other in the unaffiliated list.
     *
     * @throws Exception
     */
    @Test
    public void removeAffiliationCancelled() throws Exception {
        System.out.println("Test case for checking remove affiliation after an affiliation request is cancelled");

        this.raiseAffiliationRequest(16L, 17L);
        this.actionAffiliation("/affiliate/cancel/17", 16L, AffiliationStatus.CANCELLED);
        this.actionAffiliation("/affiliate/remove/16", 17L, AffiliationStatus.REMOVED);
        this.filterExpectValues(16L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Gabriel Mccormick");
        this.filterExpectValues(17L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Jayda Stanton");
    }

    /**
     * Affiliation Request is raised from OrganizationA to OrganizationB.
     * This request is rejected from OrganizationB.
     * As a result of this OrganizationA can see this request in ActiveRequest section.
     * OrganizationA can now choose to remove the affiliation from the ActiveRequest section.
     * After removing the affiliation request, both organizations can see each other in the unaffiliated list.
     *
     * @throws Exception
     */
    @Test
    public void removeAffiliationRejected() throws Exception {
        System.out.println("Test case for checking remove affiliation after an affiliation request is cancelled");

        this.raiseAffiliationRequest(16L, 18L);
        this.actionAffiliation("/affiliate/reject/16", 18L, AffiliationStatus.REJECTED);
        this.actionAffiliation("/affiliate/remove/18", 16L, AffiliationStatus.REMOVED);
        this.filterExpectValues(16L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Yasmine Villarreal");
        this.filterExpectValues(18L, "/affiliate/filter?", "page=0", "&offset=1",
                "&filterType=UNAFFILIATED", "&query=Jayda Stanton");
    }
}
