package com.fiap.burger.api.dto.order.request;

import com.fiap.burger.entity.order.Order;
import com.fiap.burger.entity.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.stream.Collectors;

public record OrderInsertRequestDto(
    @Null
    Long clientId,
    @NotNull
    List<OrderItemInsertRequestDto> items
) {
    public Order toEntity() {
        return new Order(
            clientId,
            items().stream().map(OrderItemInsertRequestDto::toEntity).collect(Collectors.toList()),
            OrderStatus.AGUARDANDO_PAGAMENTO
        );
    }
}