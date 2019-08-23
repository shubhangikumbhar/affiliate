/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.validator;

import com.vocera.cloud.affiliateservice.service.AffiliateService;
import com.vocera.cloud.coremodel.model.Affiliation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Affliation.
 *
 * @author Rohit Phatak
 */
@Component
public class AffiliationValidator implements Validator {

    @Autowired
    private AffiliateService affiliateService;

    /**
     * @param clazz
     * @return
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Affiliation.class == clazz;
    }

    /**
     * @param target
     * @param errors
     */
    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Affiliation) {
            Affiliation affiliation = (Affiliation) target;

            if (affiliation.getAffiliationFrom() == null || affiliation.getAffiliationFrom().getId() <= 0) {
                errors.rejectValue("affiliationFrom", "Invalid Organization to request affiliation", "Invalid " +
                        "Organization to request affiliation");
            }
            if (affiliation.getAffiliationWith() == null || affiliation.getAffiliationWith().getId() <= 0) {
                errors.rejectValue("affiliationWith", "Invalid Organization to Affiliate With", "Invalid Organization" +
                        " to Affiliate With");
            }
        }
    }
}
