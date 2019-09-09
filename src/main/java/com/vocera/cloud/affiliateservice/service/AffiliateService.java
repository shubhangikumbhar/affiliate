/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service;

import com.vocera.cloud.coremodel.constants.FilterType;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.Organization;
import com.vocera.cloud.coremodel.model.PageResponse;
import org.springframework.data.domain.Sort;

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

    /**
     * Filter through affiliates.
     *
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @return
     */
    PageResponse<Affiliation> filterAffiliation(int page, int offset, String query, String sort, Sort.Direction order,
                                                FilterType filterType, Long organizationId);

    /**
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @return
     */
    PageResponse<Organization> filterAffiliationOrganization(int page, int offset, String query, String sort,
                                                             Sort.Direction order,
                                                             FilterType filterType, Long organizationId);
}
