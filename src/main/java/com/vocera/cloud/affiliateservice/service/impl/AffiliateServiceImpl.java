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
import com.vocera.cloud.coremodel.constants.OrderableColumn;
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

    private final List<AffiliationStatus> UNAFFILIATED_FROM = Arrays.asList(AffiliationStatus.REMOVED,
            AffiliationStatus.REVOKED_FROM, AffiliationStatus.CANCELLED);
    private final List<AffiliationStatus> UNAFFILIATED_WITH = Arrays.asList(
            AffiliationStatus.REJECTED, AffiliationStatus.REMOVED, AffiliationStatus.REVOKED_TO);
    private final List<AffiliationStatus> AFFILIATES_FROM = Arrays.asList(AffiliationStatus.AFFILIATED);
    private final List<AffiliationStatus> AFFILIATES_WITH = Arrays.asList(AffiliationStatus.AFFILIATED);
    private final List<AffiliationStatus> ACTIVE_REQUEST_FROM = Arrays.asList(AffiliationStatus.ACTIVE_REQUEST,
            AffiliationStatus.REJECTED, AffiliationStatus.REVOKED_TO);
    private final List<AffiliationStatus> ACTIVE_REQUEST_WITH = Arrays.asList(AffiliationStatus.ACTIVE_REQUEST,
            AffiliationStatus.CANCELLED, AffiliationStatus.REVOKED_FROM);
    private final List<AffiliationStatus> ELEGIBLE_FOR_REMOVE = Arrays.asList(AffiliationStatus.CANCELLED,
            AffiliationStatus.REJECTED, AffiliationStatus.REVOKED_FROM, AffiliationStatus.REVOKED_TO,
            AffiliationStatus.UNREGISTERED_TO, AffiliationStatus.UNREGISTERED_FROM);

    private final List<AffiliationStatus> AFFILIATION_REQUEST_CHECK_FROM = Arrays.asList(AffiliationStatus.CANCELLED,
            AffiliationStatus.REVOKED_FROM);
    private final List<AffiliationStatus> AFFILIATION_REQUEST_CHECK_WITH = Arrays.asList(AffiliationStatus.REJECTED,
            AffiliationStatus.REVOKED_TO);

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
    @Transactional
    public Affiliation affiliate(Affiliation affiliationRequest, Long organizationId) {
        Affiliation affiliationState = this.checkAffiliation(
                affiliationRequest.getAffiliationFrom().getId(),
                affiliationRequest.getAffiliationWith().getId());
        if (affiliationState.getStatus() != AffiliationStatus.NONE &&
                !(organizationId == affiliationState.getAffiliationWith().getId() &&
                        AFFILIATION_REQUEST_CHECK_WITH.contains(affiliationState.getStatus())) &&
                !(organizationId == affiliationState.getAffiliationFrom().getId() &&
                        AFFILIATION_REQUEST_CHECK_FROM.contains(affiliationState.getStatus()))) {
            return affiliationState;
        } else {
            if ((AFFILIATION_REQUEST_CHECK_WITH.contains(affiliationState.getStatus()) &&
                    organizationId == affiliationState.getAffiliationWith().getId()) ||
                    (AFFILIATION_REQUEST_CHECK_FROM.contains(affiliationState.getStatus()) &&
                            organizationId == affiliationState.getAffiliationFrom().getId())) {
                this.removeAffiliation(affiliationRequest.getAffiliationFrom().getId(),
                        affiliationRequest.getAffiliationWith().getId());
            }
            affiliationRequest.setActive(true);
            return this.affiliateRepository.saveAndFlush(affiliationRequest);
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
    public PageResponse<Affiliation> filterAffiliation(
            int page, int offset, String query, OrderableColumn sort, Sort.Direction order,
            FilterType filterType, Long organizationId) {
        Page<Affiliation> affiliationPage;
        switch (filterType) {
            case UNAFFILIATED:
                affiliationPage = affiliateRepository.findAll(AffiliateSpecifications.affiliatesSpecification(
                        true, query, sort, order, organizationId, UNAFFILIATED_FROM, UNAFFILIATED_WITH),
                        new PageRequest(page, offset));
                break;
            case AFFILIATES:
                affiliationPage = affiliateRepository.findAll(AffiliateSpecifications.affiliatesSpecification(
                        true, query, sort, order, organizationId, AFFILIATES_FROM, AFFILIATES_WITH),
                        new PageRequest(page, offset));
                break;
            case ACTIVE_REQUESTS:
                affiliationPage = affiliateRepository.findAll(AffiliateSpecifications.affiliatesSpecification(
                        true, query, sort, order, organizationId, ACTIVE_REQUEST_FROM, ACTIVE_REQUEST_WITH),
                        new PageRequest(page, offset));
                break;
            case ALL:
            default:
                affiliationPage = affiliateRepository.findAll(AffiliateSpecifications.affiliatesSpecification(
                        true, query, sort, order, organizationId,
                        Arrays.asList(AffiliationStatus.values()),
                        Arrays.asList(AffiliationStatus.values())),
                        new PageRequest(page, offset));
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
            int page, int offset, String query, OrderableColumn sort, Sort.Direction order, FilterType filterType,
            Long organizationId) {
        PageResponse<Affiliation> pageResponse = this.filterAffiliation(page, offset, query, sort, order, filterType,
                organizationId);
        return new PageResponse<Organization>(pageResponse.getData().stream()
                .map(affiliation -> affiliation.getAffiliationWith()).collect(Collectors.toList()),
                page, offset, pageResponse.getTotalCount());

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

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) &&
                affiliation.getAffiliationWith().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.AFFILIATED, true);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Approve Request Failed",
                Arrays.asList("Invalid affiliation to approve !!"));
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

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) &&
                affiliation.getAffiliationWith().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.REJECTED, true);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Reject Request Failed",
                Arrays.asList("Invalid affiliation to reject !!"));
    }

    /**
     * Check if there is an affiliation request in ACTIVE_REQUEST state.
     * Check if AffiliatedFrom organization is the same organization requesting for an approval.
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

        if (affiliation.getStatus().equals(AffiliationStatus.ACTIVE_REQUEST) &&
                affiliation.getAffiliationFrom().getId() == organizationId) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.CANCELLED, true);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Cancel Request Failed",
                Arrays.asList("Invalid affiliation to cancel !!"));
    }

    /**
     * Check if there is an affiliation request in AFFILIATED state.
     * If yes check for the organizationId with affiliationFrom.
     * If organizationId matches organizationId of affiliationFrom set status to REVOKED_FROM.
     * Else set status to REVOKED_TO.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    @Override
    @Transactional
    public Affiliation revokeAffiliation(Long organizationId, Long affiliatedOrganizationId) {
        Affiliation affiliation = this.checkAffiliation(organizationId, affiliatedOrganizationId);

        if (affiliation.getStatus().equals(AffiliationStatus.AFFILIATED)) {
            int result = affiliateRepository.updateStatus(affiliation.getId(),
                    (affiliation.getAffiliationFrom().getId() == organizationId) ? AffiliationStatus.REVOKED_FROM :
                            AffiliationStatus.REVOKED_TO, true);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Revoke Request Failed",
                Arrays.asList("Invalid affiliation to revoke !!"));
    }

    /**
     * Set the affiliation status to REMOVED and set active = false.
     *
     * @param organizationId
     * @param affiliatedOrganizationId
     * @return
     */
    @Override
    @Transactional
    public Affiliation removeAffiliation(Long organizationId, Long affiliatedOrganizationId) {
        Affiliation affiliation = this.checkAffiliation(organizationId, affiliatedOrganizationId);

        if (ELEGIBLE_FOR_REMOVE.contains(affiliation.getStatus())) {
            int result = affiliateRepository.updateStatus(affiliation.getId(), AffiliationStatus.REMOVED, false);
            if (result > 0) {
                return affiliateRepository.findById(affiliation.getId()).get();
            }
        }
        throw new InvalidAffiliationException("Revoke Request Failed",
                Arrays.asList("Invalid affiliation to revoke !!"));
    }
}
