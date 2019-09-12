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

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        Affiliation affiliationState = this.checkAffiliation(
                affiliationRequest.getAffiliationFrom().getId(),
                affiliationRequest.getAffiliationWith().getId());
        if (affiliationState.getStatus() != AffiliationStatus.NONE) {
            return affiliationState;
        } else {
            affiliationRequest.setActive(true);
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

    /**
     * Check if two organizations are affiliated.
     *
     * @param affiliationFrom
     * @param affiliationWith
     * @return
     */
    @Override
    public Affiliation checkAffiliation(Long affiliationFrom, Long affiliationWith) {
        Optional<Affiliation> affiliationResponse = affiliateRepository.checkAffiliation(affiliationFrom,
                affiliationWith);
        if (affiliationResponse.isPresent()) {
            return affiliationResponse.get();
        } else {
            Affiliation affiliation = new Affiliation();
            affiliation.setStatus(AffiliationStatus.NONE);
            affiliation.setAffiliationFrom(new Organization(affiliationFrom));
            affiliation.setAffiliationWith(new Organization(affiliationWith));
            return affiliation;
        }
    }

    /**
     * Check if there is an affiliation request in ACTIVE_REQUEST state.
     * Check if AffiliatedWith organization is the same organization requesting for an approval.
     * If yes, update the affiliation status to AFFILIATED.
     * Else, return affiliation.
     *
     * @param organizationId
     * @param requestingOrganizationId
     * @return
     */
    @Override
    @Transactional
    public Affiliation approveAffiliation(Long organizationId, Long requestingOrganizationId) {
        Affiliation affiliation = this.checkAffiliation(organizationId, requestingOrganizationId);

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) && affiliation.getAffiliationWith().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.AFFILIATED);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Approve Request Failed", Arrays.asList("Invalid affiliation to approve !!"));
    }

    /**
     * Check if there is an affiliation request in ACTIVE_REQUEST state.
     * Check if AffiliatedWith organization is the same organization requesting for an approval.
     * If yes, update the affiliation status to REJECTED.
     * Else, return affiliation.
     *
     * @param organizationId
     * @param requestingOrganizationId
     * @return
     */
    @Override
    @Transactional
    public Affiliation rejectAffiliation(Long organizationId, Long requestingOrganizationId) {
        Affiliation affiliation = this.checkAffiliation(organizationId, requestingOrganizationId);

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) && affiliation.getAffiliationWith().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.REJECTED);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Reject Request Failed", Arrays.asList("Invalid affiliation to reject !!"));
    }

    /**
     * Check if there is an affiliation request in ACTIVE_REQUEST state.
     * Check if AffiliatedWith organization is the same organization requesting for an approval.
     * If yes, update the affiliation status to CANCELLED.
     * Else, return affiliation.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    @Override
    @Transactional
    public Affiliation cancelAffiliation(Long organizationId, Long affiliatedOrganizationId) {
        Affiliation affiliation = this.checkAffiliation(organizationId, affiliatedOrganizationId);

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) && affiliation.getAffiliationFrom().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.CANCELLED);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Cancel Request Failed", Arrays.asList("Invalid affiliation to cancel !!"));
    }
}
