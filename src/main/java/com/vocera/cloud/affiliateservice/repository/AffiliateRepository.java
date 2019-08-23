/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.repository;

import com.vocera.cloud.coremodel.model.Affiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repository for Affiliation.
 *
 * @author Rohit Phatak
 */
public interface AffiliateRepository extends JpaRepository<Affiliation, Long> {

    /**
     * Check if two organizations are Affiliated.
     * Return Affiliation details in case both organizations are affiliated.
     *
     * @param organization1
     * @param organization2
     * @return
     */
    @Query("select a from Affiliation a where " +
            "(a.affiliationFrom.id=?1 and a.affiliationWith.id=?2) or " +
            "(a.affiliationFrom.id=?2 and a.affiliationWith.id=?1)")
    Optional<Affiliation> checkAffiliation(Long organization1, Long organization2);
}
