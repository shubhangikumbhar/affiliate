/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service.impl;

import com.vocera.cloud.affiliateservice.exception.InvalidAffiliationException;
import com.vocera.cloud.affiliateservice.repository.AffiliateRepository;
import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.coremodel.constants.AffiliationStatus;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test cases for {@link AffiliateService}
 *
 * @author Rohit Phatak
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
class AffiliateServiceImplTest {

    /**
     * Configuration class
     */
    @TestConfiguration
    static class AffiliateServiceImplTestConfig {

        @Bean
        public AffiliateService affiliateService() {
            return new AffiliateServiceImpl(affiliateRepository);
        }

        @Autowired
        public AffiliateRepository affiliateRepository;
    }

    @Autowired
    private AffiliateService affiliateService;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Test case for successful affiliation request.
     */
    @Test
    public void successfulAffiliation() {
        System.out.println("Test case for successful affiliation.");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(1l, 2l, AffiliationStatus.ACTIVE_REQUEST), 1l);

        assertTrue(affiliationResponse.getId() > 0);
    }

    /**
     * Test case for affiliation already present.
     */
    @Test
    public void affiliationAlreadyPresent() {
        System.out.println("Test case for affiliation already present");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(1l, 2l, AffiliationStatus.ACTIVE_REQUEST), 1l);
        Affiliation affiliationResponseDup = affiliateService.affiliate(
                this.createAffiliation(1l, 2l, AffiliationStatus.ACTIVE_REQUEST), 1l);

        assertEquals(affiliationResponse.getId(), affiliationResponseDup.getId());
    }

    /**
     * Test case for checking if two organizations are affiliated.
     */
    @Test
    public void checkAffiliation() {
        System.out.println("Test case for checking if organizations are already affiliated.");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(2l, 3l, AffiliationStatus.ACTIVE_REQUEST), 2l);
        Affiliation affiliation = this.affiliateService.checkAffiliation(2l, 3l);
        assertEquals(AffiliationStatus.ACTIVE_REQUEST, affiliation.getStatus());
    }

    /**
     * Test case for checking if AffiliationStatus is NONE if two organizations are not affiliated.
     */
    @Test
    public void checkAffiliationNone() {
        System.out.println("Test case for checking if AffiliationStatus is NONE if two organizations are not " +
                "affiliated");

        Affiliation affiliation = this.affiliateService.checkAffiliation(1l, 4l);
        assertEquals(AffiliationStatus.NONE, affiliation.getStatus());
    }

    /**
     * Test case for approving an affiliation request
     */
    @Test
    public void approveAffiliationTest() {
        System.out.println("Test case for approving an affiliation request");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(3l, 2l, AffiliationStatus.ACTIVE_REQUEST), 3l);
        Affiliation affiliation = this.affiliateService.approveAffiliation(2l, 3l);
        assertEquals(AffiliationStatus.AFFILIATED, affiliation.getStatus());
    }

    /**
     * Failure test case for approving an affiliation request
     */
    @Test
    public void approveAffiliationTestInvalid() {
        System.out.println("Failure test case for approving an affiliation request");

        assertThrows(InvalidAffiliationException.class, () -> {
            this.affiliateService.approveAffiliation(3l, 4l);
        });
    }

    /**
     * Test case for rejecting an affiliation request
     */
    @Test
    public void rejectAffiliationTest() {
        System.out.println("Test case for rejecting an affiliation request");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(3l, 2l, AffiliationStatus.ACTIVE_REQUEST), 3l);
        Affiliation affiliation = this.affiliateService.rejectAffiliation(2l, 3l);
        assertEquals(AffiliationStatus.REJECTED, affiliation.getStatus());
    }

    /**
     * Failure test case for rejecting an affiliation request
     */
    @Test
    public void rejectAffiliationTestInvalid() {
        System.out.println("Failure test case for rejecting an affiliation request");

        assertThrows(InvalidAffiliationException.class, () -> {
            this.affiliateService.rejectAffiliation(3l, 4l);
        });
    }

    /**
     * Test case for cancelling an affiliation request
     */
    @Test
    public void cancelAffiliationTest() {
        System.out.println("Test case for cancelling an affiliation request");

        Affiliation affiliationResponse = affiliateService.affiliate(
                this.createAffiliation(3l, 2l, AffiliationStatus.ACTIVE_REQUEST), 3l);
        Affiliation affiliation = this.affiliateService.cancelAffiliation(3l, 2l);
        assertEquals(AffiliationStatus.CANCELLED, affiliation.getStatus());
    }

    /**
     * Failure test case for cancelling an affiliation request
     */
    @Test
    public void cancelAffiliationTestInvalid() {
        System.out.println("Failure test case for cancelling an affiliation request");

        assertThrows(InvalidAffiliationException.class, () -> {
            this.affiliateService.cancelAffiliation(3l, 4l);
        });
    }

    /**
     * Helper method for creating an affiliation.
     *
     * @param org1
     * @param org2
     * @param status
     * @return
     */
    private Affiliation createAffiliation(long org1, long org2, AffiliationStatus status) {
        Affiliation affiliation = new Affiliation();
        affiliation.setAffiliationFrom(new Organization(org1));
        affiliation.setAffiliationWith(new Organization(org2));
        affiliation.setStatus(status);
        return affiliation;
    }

    @Test
    public void testAffiliationRequestAfterCancellation() {
        System.out.println("Test case for raising an affiliation request after a request is cancelled.");

        Affiliation firstAffRequest = this.createAffiliation(4l, 5l, AffiliationStatus.ACTIVE_REQUEST);
        this.affiliateService.affiliate(firstAffRequest, 4l);
        assertEquals(AffiliationStatus.ACTIVE_REQUEST, firstAffRequest.getStatus());
        Affiliation affiliation = this.affiliateService.cancelAffiliation(4l, 5l);
        assertEquals(AffiliationStatus.CANCELLED, affiliation.getStatus());
        Affiliation affRequest = this.createAffiliation(5l, 4l, AffiliationStatus.ACTIVE_REQUEST);
        assertEquals(AffiliationStatus.ACTIVE_REQUEST, affRequest.getStatus());
    }

}