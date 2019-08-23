/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.controller;

import com.vocera.cloud.affiliateservice.exception.InvalidAffiliationException;
import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.affiliateservice.validator.AffiliationValidator;
import com.vocera.cloud.coremodel.model.Affiliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
