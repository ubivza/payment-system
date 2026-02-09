package com.example.fakepaymentprovider.repository.specification;

import com.example.fakepaymentprovider.entity.BaseEntity;
import com.example.fakepaymentprovider.entity.BaseEntity_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public class BaseSpecification<T extends BaseEntity> implements Specification<T> {
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private UUID merchantId;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.conjunction();

        if (merchantId != null) {
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get(BaseEntity_.MERCHANT_ID), merchantId));
        }

        if (startDate != null) {
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get(BaseEntity_.CREATED_AT), startDate.toInstant()));
        }

        if (endDate != null) {
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get(BaseEntity_.CREATED_AT), endDate.toInstant()));
        }

        return predicate;
    }
}

