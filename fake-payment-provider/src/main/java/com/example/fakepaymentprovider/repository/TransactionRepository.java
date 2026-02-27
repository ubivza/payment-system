package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndMerchantId(UUID id, UUID merchantId);

    @Query("""
            select t from Transaction t
            where t.status in :statuses
              and not exists (
                  select 1 from Webhook w
                  where w.eventType = 'transaction'
                    and w.entityId = t.id
              )
            order by t.updatedAt asc
            """)
    List<Transaction> findReadyUnsent(@Param("statuses") Collection<String> statuses, org.springframework.data.domain.Pageable pageable);
}
