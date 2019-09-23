/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service;

import com.vocera.cloud.coremodel.constants.FilterType;
import com.vocera.cloud.coremodel.constants.OrderableColumn;
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
    Affiliation affiliate(Affiliation affiliation, Long organizationId);

    /**
     * Filter through affiliates.
     *
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @param organizationId
     * @return
     */
    PageResponse<Affiliation> filterAffiliation(
            int page, int offset, String query, OrderableColumn sort, Sort.Direction order,
            FilterType filterType, Long organizationId);

    /**
     * Filter through organizations in affiliation.
     *
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @param organizationId
     * @return
     */
    PageResponse<Organization> filterAffiliationOrganization(
            int page, int offset, String query, OrderableColumn sort, Sort.Direction order,
            FilterType filterType, Long organizationId);

    /**
     * Check if two organizations are affiliated.
     *
     * @param org1
     * @param org2
     * @return
     */
    Affiliation checkAffiliation(Long org1, Long org2);

    /**
     * Approve an affiliation request.
     *
     * @param organizationId
     * @param requestingOrganizationId
     * @return
     */
    Affiliation approveAffiliation(Long organizationId, Long requestingOrganizationId);

    /**
     * Reject an affiliation request.
     *
     * @param organizationId
     * @param requestingOrganizationId
     * @return
     */
    Affiliation rejectAffiliation(Long organizationId, Long requestingOrganizationId);

    /**
     * Cancel an affiliation request sent.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    Affiliation cancelAffiliation(Long organizationId, Long affiliatedOrganizationId);

    /**
     * Cancel an affiliation request sent.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    Affiliation revokeAffiliation(Long organizationId, Long affiliatedOrganizationId);

    /**
     * Remove an affiliation between two organizations.
     * This will set the affiliation to active false.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    Affiliation removeAffiliation(Long organizationId, Long affiliatedOrganizationId);
}
