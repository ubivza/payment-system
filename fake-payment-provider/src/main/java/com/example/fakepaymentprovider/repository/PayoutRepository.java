package com.example.fakepaymentprovider.repository;

import com.example.fakepaymentprovider.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends JpaRepository<Payout, UUID>, JpaSpecificationExecutor<Payout> {
    Optional<Payout> findByIdAndMerchantId(UUID id, UUID merchantId);

    @Query("""
            select p from Payout p
            where p.status in :statuses
              and not exists (
                  select 1 from Webhook w
                  where w.eventType = 'payout'
                    and w.entityId = p.id
              )
            order by p.updatedAt asc
            """)
    List<Payout> findReadyUnsent(@Param("statuses") Collection<String> statuses, org.springframework.data.domain.Pageable pageable);
}
