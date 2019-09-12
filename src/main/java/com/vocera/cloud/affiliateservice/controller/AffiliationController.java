/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.controller;

import com.vocera.cloud.affiliateservice.exception.InvalidAffiliationException;
import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.affiliateservice.validator.AffiliationValidator;
import com.vocera.cloud.coremodel.constants.FilterType;
import com.vocera.cloud.coremodel.constants.HttpHeader;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.Organization;
import com.vocera.cloud.coremodel.model.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Affiliation REST api Controller.
 *
 * @author Rohit Phatak
 */
@RestController
@RequestMapping("/affiliate")
public class AffiliationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AffiliationController.class);

    private AffiliateService affiliateService;

    private AffiliationValidator affiliationValidator;

    /**
     * Constructor
     *
     * @param affiliateService
     * @param affiliationValidator
     */
    public AffiliationController(AffiliateService affiliateService, AffiliationValidator affiliationValidator) {
        this.affiliateService = affiliateService;
        this.affiliationValidator = affiliationValidator;
    }

    /**
     * Init Binder.
     *
     * @param binder
     */
    @InitBinder("affiliation")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(affiliationValidator);
    }

    /**
     * Request for an Affiliation.
     * AffiliateValidator is used for the purpose of validation of the affiliate object.
     *
     * @param affiliation
     * @param bindingResult
     * @return
     * @throws Exception
     */
    @PostMapping("")
    public ResponseEntity<Affiliation> affiliate(@Valid @RequestBody Affiliation affiliation,
                                                 BindingResult bindingResult) throws Exception {

        Affiliation affiliate;
        if (bindingResult.hasErrors()) {
            LOGGER.info("Invalid Request for Affiliation");
            throw new InvalidAffiliationException("Error Processing Affiliate Request", bindingResult.getAllErrors());
        } else {
            LOGGER.info("Affiliation Requested from {} to {}", affiliation.getAffiliationFrom().getId(),
                    affiliation.getAffiliationWith().getId());
            affiliate = affiliateService.affiliate(affiliation);
        }
        return new ResponseEntity<>(affiliate, HttpStatus.OK);
    }

    /**
     * Filter through affiliations.
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
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<Affiliation>> filterAffiliation(
            @RequestParam("page") int page,
            @RequestParam("offset") int offset,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
            @RequestParam(value = "order", required = false, defaultValue = "") Sort.Direction order,
            @RequestParam(value = "filterType", required = false, defaultValue = "ALL") FilterType filterType,
            @RequestHeader(HttpHeader.ORGANIZATION_ID) Long organizationId) {
        LOGGER.info("Filter affiliation called on Page:{}, Offset:{}, query:{}, sort:{}, order:{}, filterType:{} by " +
                "{}", page, offset, query, sort, order, filterType, organizationId);

        return new ResponseEntity<>(affiliateService.filterAffiliation(page, offset, query, sort, order, filterType,
                organizationId), HttpStatus.OK);
    }


    /**
     * Filter through organizations in Affiliations.
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
    @GetMapping("/organization/filter")
    public ResponseEntity<PageResponse<Organization>> filterAffiliationOrganization(
            @RequestParam("page") int page,
            @RequestParam("offset") int offset,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
            @RequestParam(value = "order", required = false, defaultValue = "") Sort.Direction order,
            @RequestParam(value = "filterType", required = false, defaultValue = "ALL") FilterType filterType,
            @RequestHeader(HttpHeader.ORGANIZATION_ID) Long organizationId) {
        LOGGER.info("Filter organization called on Page:{}, Offset:{}, query:{}, sort:{}, order:{}, filterType:{} by " +
                "{}", page, offset, query, sort, order, filterType, organizationId);

        return new ResponseEntity<>(affiliateService.filterAffiliationOrganization(page, offset, query, sort, order,
                filterType, organizationId), HttpStatus.OK);
    }

    /**
     * Approve an affiliation request.
     *
     * @param organizationId
     * @param affiliateWith
     * @return
     */
    @PostMapping("/approve/{organizationId}")
    public ResponseEntity<Affiliation> approveAffiliation(
            @RequestHeader(HttpHeader.ORGANIZATION_ID) Long organizationId,
            @PathVariable("organizationId") Long affiliateWith) {
        LOGGER.info("Approve affiliation request called on {}", organizationId);
        return new ResponseEntity<>(this.affiliateService.approveAffiliation(organizationId, affiliateWith),
                HttpStatus.OK);
    }

    /**
     * Reject an affiliation request.
     *
     * @param organizationId
     * @param affiliateWith
     * @return
     */
    @PostMapping("/reject/{organizationId}")
    public ResponseEntity<Affiliation> rejectAffiliation(
            @RequestHeader(HttpHeader.ORGANIZATION_ID) Long organizationId,
            @PathVariable("organizationId") Long affiliateWith) {
        LOGGER.info("Reject affiliation request called on {}", organizationId);
        return new ResponseEntity<>(this.affiliateService.rejectAffiliation(organizationId, affiliateWith),
                HttpStatus.OK);
    }

    /**
     * Cancel an affiliation request sent.
     *
     * @param organizationId
     * @param affiliateWith
     * @return
     */
    @PostMapping("/cancel/{organizationId}")
    public ResponseEntity<Affiliation> cancelAffiliation(
            @RequestHeader(HttpHeader.ORGANIZATION_ID) Long organizationId,
            @PathVariable("organizationId") Long affiliateWith) {
        LOGGER.info("Cancel affiliation request called on {}", organizationId);
        return new ResponseEntity<>(this.affiliateService.cancelAffiliation(organizationId, affiliateWith),
                HttpStatus.OK);
    }
}
