/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice;

import com.vocera.cloud.coremodel.model.Affiliation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Entry Point for Affiliate Service.
 *
 * @author Rohit Phatak
 */
@SpringBootApplication
@EntityScan(basePackageClasses = Affiliation.class)
public class AffiliateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AffiliateServiceApplication.class, args);
    }

}
