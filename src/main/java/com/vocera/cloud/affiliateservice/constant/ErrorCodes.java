/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.constant;

/**
 * Enum constants.
 *
 * @author Rohit Phatak
 */
public enum ErrorCodes {
    UNKNOWN_EXECEPTION("1000", ""),
    CONSTRAINT_VIOLATION("1001", ""),
    INVALID_AFFILIATION_WITH("1002", "Invalid Organization to affiliate with"),
    INVALID_REQUEST("1003", "Invalid Request"),
    INVALID_AFFILIATION_REQUEST("1004", "Error Raising Affiliation Request");

    private final String code;

    private final String message;

    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
