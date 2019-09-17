/*
 * Copyright (c) Vocera Communications, Inc. All Rights Reserved.
 * This software is the confidential and proprietary information of
 * Vocera Communications, Inc.
 */

package com.vocera.cloud.affiliateservice.service.impl;

import com.vocera.cloud.coremodel.constants.AffiliationStatus;
import com.vocera.cloud.coremodel.constants.OrderableColumn;
import com.vocera.cloud.coremodel.model.Affiliation;
import com.vocera.cloud.coremodel.model.Organization;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * Create Specifications for filtering Affiliations.
 *
 * @author Rohit Phatak
 */
public class AffiliateSpecifications {

    /**
     * Specification for filtering through affiliates.
     *
     * @param active
     * @param queryString
     * @param sort
     * @param order
     * @param organizationId
     * @param affiliationFrom
     * @param affiliationWith
     * @return
     */
    public static Specification<Affiliation> affiliatesSpecification(
            boolean active, String queryString, OrderableColumn sort, Sort.Direction order,
            Long organizationId, List<AffiliationStatus> affiliationFrom,
            List<AffiliationStatus> affiliationWith) {
        return Specification.where(filterActiveAndOrder(active, sort, organizationId, order))
                .and(organizationFrom(organizationId).and(statusIn(affiliationFrom).and(
                        nameLike(queryString, "affiliationWith")
                                .or(healthSystemNameLike(queryString, "affiliationWith"))))
                        .or(organizationWith(organizationId).and(statusIn(affiliationWith)).and(
                                nameLike(queryString, "affiliationFrom")
                                        .or(healthSystemNameLike(queryString, "affiliationFrom")))));
    }

    /**
     * Specification for filtering through active affiliations and ordering them.
     *
     * @param active
     * @param sortColumn
     * @param orgId
     * @param order
     * @return
     */
    public static Specification<Affiliation> filterActiveAndOrder(
            boolean active, OrderableColumn sortColumn, Long orgId, Sort.Direction order) {
        return (root, query, criteriaBuilder) -> {
            Join<Affiliation, Organization> organizationFromJoin = root.join("affiliationFrom");
            Join<Affiliation, Organization> organizationWithJoin = root.join("affiliationWith");
            Predicate activePredicate = criteriaBuilder.equal(root.get("active"), active);
            Expression organizationIdExpr = criteriaBuilder.equal(organizationFromJoin.get("id"), orgId);
            query.orderBy(new OrderImpl(criteriaBuilder.selectCase().when(organizationIdExpr,
                    organizationWithJoin.get(sortColumn.getName())
            ).otherwise(organizationFromJoin.get(sortColumn.getName())),
                    order.equals(Sort.Direction.ASC)));
            return activePredicate;
        };
    }

    /**
     * Specification for filtering organizationFrom based on organizationId.
     *
     * @param id
     * @return
     */
    public static Specification<Affiliation> organizationFrom(Long id) {
        return (root, query, criteriaBuilder) -> {
            Join<Affiliation, Organization> organizationFromJoin = root.join("affiliationFrom");
            return criteriaBuilder.and(criteriaBuilder.equal(organizationFromJoin.get("id"), id));
        };
    }

    /**
     * Specification for filtering organizationWith based on organizationId.
     *
     * @param id
     * @return
     */
    public static Specification<Affiliation> organizationWith(Long id) {
        return (root, query, criteriaBuilder) -> {
            Join<Affiliation, Organization> organizationWithJoin = root.join("affiliationWith");
            Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(organizationWithJoin.get("id"), id));
            return predicate;
        };
    }

    /**
     * Specification for filtering through AffiliationStatus.
     *
     * @param affiliationStatusList
     * @return
     */
    public static Specification<Affiliation> statusIn(List<AffiliationStatus> affiliationStatusList) {
        return (root, query, criteriaBuilder) -> root.get("status").in(affiliationStatusList);
    }

    /**
     * Specification for filtering name with like operator.
     *
     * @param q
     * @param joinAttribute
     * @return
     */
    public static Specification<Affiliation> nameLike(String q, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            Join<Affiliation, Organization> organizationJoin = root.join(joinAttribute);
            return criteriaBuilder.like(organizationJoin.get("name"), "%" + q + "%");
        };
    }

    /**
     * Specification for filtering healthSystemName with like operator.
     *
     * @param q
     * @param joinAttribute
     * @return
     */
    public static Specification<Affiliation> healthSystemNameLike(String q, String joinAttribute) {
        return (root, query, criteriaBuilder) -> {
            Join<Affiliation, Organization> organizationJoin = root.join(joinAttribute);
            return criteriaBuilder.or(criteriaBuilder.like(organizationJoin.get("healthSystemName"),
                    "%" + q + "%"));
        };
    }
}
