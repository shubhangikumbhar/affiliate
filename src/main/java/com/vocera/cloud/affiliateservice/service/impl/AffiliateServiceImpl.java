/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service.impl;

import com.vocera.cloud.affiliateservice.repository.AffiliateRepository;
import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.coremodel.constants.AffiliationStatus;
import com.vocera.cloud.coremodel.constants.FilterType;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.Organization;
import com.vocera.cloud.coremodel.model.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

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
            return affiliateRepository.saveAndFlush(affiliationRequest);
        }

    }

    /**
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @param organizationId
     * @return
     */
    @Override
    public PageResponse<Affiliation> filterAffiliation(int page, int offset, String query, String sort,
                                                       Sort.Direction order,
                                                       FilterType filterType, Long organizationId) {
        Page<Affiliation> affiliationPage;
        switch (filterType) {
            // TODO Affiliated
            case UNAFFILIATED:
                affiliationPage = affiliateRepository.affiliated(organizationId, AffiliationStatus.AFFILIATED,
                        new PageRequest(page, offset));
                break;
            case AFFILIATES:
                affiliationPage = affiliateRepository.affiliates(organizationId, AffiliationStatus.AFFILIATED,
                        new PageRequest(page, offset));
                break;
            case ACTIVE_REQUESTS:
                affiliationPage = affiliateRepository.activeRequests(organizationId, AffiliationStatus.ACTIVE_REQUEST,
                        new PageRequest(page, offset));
                break;
            case ALL:
            default:
                affiliationPage = affiliateRepository.findAll(new PageRequest(page, offset));
        }
        return new PageResponse<Affiliation>(affiliationPage.getContent(), page, offset,
                affiliationPage.getTotalElements());
    }

    /**
     * @param page
     * @param offset
     * @param query
     * @param sort
     * @param order
     * @param filterType
     * @param organizationId
     * @return
     */
    @Override
    public PageResponse<Organization> filterAffiliationOrganization(
            int page, int offset, String query, String sort, Sort.Direction order, FilterType filterType,
            Long organizationId) {
        PageResponse<Affiliation> pageResponse = this.filterAffiliation(page, offset, query, sort, order, filterType,
                organizationId);
        return new PageResponse<Organization>(pageResponse.getData().stream().map(affiliation -> {
            return affiliation.getAffiliationWith();
        }).collect(Collectors.toList()), page, offset, pageResponse.getTotalCount());
    }
}
