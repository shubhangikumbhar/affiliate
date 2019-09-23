/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.exception;

import java.util.List;

/**
 * Invalid Affiliation Request Exception.
 *
 * @author Rohit Phatak
 */
public class InvalidAffiliationException extends RuntimeException {

    private final List<Object> errors;

    /**
     * Constructor.
     *
     * @param message
     * @param errors
     */
    public InvalidAffiliationException(String message, List errors) {
        super(message);
        this.errors = errors;
    }

    public List<Object> getErrors() {
        return errors;
    }
}
