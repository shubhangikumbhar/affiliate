/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service;

import com.vocera.cloud.coremodel.model.Affiliation;

/**
 * Affiliation Service.
 *
 * @author Rohit Phatak
 */
public interface AffiliateService {

    /**
     * Request for Affiliation.
     *
     * @param affiliation
     * @return
     */
    Affiliation affiliate(Affiliation affiliation);
}
