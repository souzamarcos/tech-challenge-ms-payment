package com.fiap.burger.persistence.order.dao;

import com.fiap.burger.domain.entities.order.OrderStatus;
import com.fiap.burger.persistence.order.model.OrderJPA;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDAO extends JpaRepository<OrderJPA, Long> {

    List<OrderJPA> findAllByDeletedAtNull();

    List<OrderJPA> findAllByDeletedAtNullAndStatusEquals(OrderStatus status);

    @Query("select o from OrderJPA o where o.deletedAt = null and o.status in :status order by o.status desc, o.modifiedAt")
    List<OrderJPA> findAllInProgress(@Param("status") Set<OrderStatus> status);

    @Modifying
    @Query("update OrderJPA o set o.status = :newStatus, o.modifiedAt = :modifiedAt where o.id = :id")
    void updateStatus(@Param("id") Long id, @Param("newStatus") OrderStatus newStatus, @Param("modifiedAt") LocalDateTime modifiedAt);

}
