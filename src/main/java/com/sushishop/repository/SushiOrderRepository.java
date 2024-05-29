package com.sushishop.repository;

import java.util.List;

import com.sushishop.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sushishop.entity.SushiOrder;

@Repository
public interface SushiOrderRepository extends JpaRepository<SushiOrder, Long>{

    /**
     * Find order by status id
     *
     * @param statusId
     * @return
     */
    List<SushiOrder> findByStatusId(int statusId);

    /**
     * Update order status by order id
     *
     * @param orderId
     * @param status
     */
    @Query("update SushiOrder set status = :status where id = :orderId")
    void updateOrderStatus(Long orderId, Status status);
}
