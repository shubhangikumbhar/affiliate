/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service.impl;

import com.vocera.cloud.affiliateservice.repository.AffiliateRepository;
import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.coremodel.model.Affiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation for @{@link AffiliateService}.
 *
 * @author Rohit Phatak
 */
@Service
public class AffiliateServiceImpl implements AffiliateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffiliateServiceImpl.class);

    private AffiliateRepository affiliateRepository;

    public AffiliateServiceImpl(AffiliateRepository affiliateRepository) {
        this.affiliateRepository = affiliateRepository;
    }

    /**
     * Request an affiliation.
     *
     * @param affiliationRequest
     * @return
     */
    @Override
    public Affiliation affiliate(Affiliation affiliationRequest) {
        Optional<Affiliation> affiliationState = affiliateRepository.checkAffiliation(
                affiliationRequest.getAffiliationFrom().getId(),
                affiliationRequest.getAffiliationWith().getId());
        if (affiliationState.isPresent()) {
            return affiliationState.get();
        } else {
            return affiliateRepository.save(affiliationRequest);
        }

    }
}
