package com.fiap.burger.usecase.usecase;

import com.fiap.burger.entity.order.Order;
import com.fiap.burger.entity.order.OrderStatus;
import com.fiap.burger.entity.payment.Payment;
import com.fiap.burger.entity.payment.PaymentStatus;
import com.fiap.burger.usecase.adapter.gateway.PaymentGateway;
import com.fiap.burger.usecase.adapter.usecase.OrderUseCase;
import com.fiap.burger.usecase.adapter.usecase.PaymentUseCase;
import com.fiap.burger.usecase.misc.exception.InvalidAttributeException;
import com.fiap.burger.usecase.misc.exception.OrderCannotBePaidException;
import com.fiap.burger.usecase.misc.exception.PaymentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


public class DefaultPaymentUseCase implements PaymentUseCase {

    private final PaymentGateway paymentGateway;

    private final OrderUseCase orderUseCase;

    public DefaultPaymentUseCase(PaymentGateway paymentGateway, OrderUseCase orderUseCase) {
        this.paymentGateway = paymentGateway;
        this.orderUseCase = orderUseCase;
    }

    public Payment findById(Long id) {
        return paymentGateway.findById(id);
    }

    public List<Payment> findByOrderId(Long orderId) {
        return paymentGateway.findByOrderId(orderId);
    }

    public Payment insert(Long orderId) {

        Order order = orderUseCase.findById(orderId);

        if (!orderUseCase.canBePaid(order.getStatus())) {
            throw new OrderCannotBePaidException(orderId);
        }

        return paymentGateway.save(Payment.createPaymentWithOrderAndOpenStatus(order));
    }

    @Override
    public void updateStatus(Long id, PaymentStatus status) {
        Payment persistedPayment = findById(id);

        if (persistedPayment == null) {
            throw new PaymentNotFoundException(id);
        }

        validateUpdateStatus(status, persistedPayment.getStatus());

        paymentGateway.updatePaymentStatus(id, status, LocalDateTime.now());

        switch (status) {
            case APROVADO -> orderUseCase.checkout(persistedPayment.getOrder().getId());
            case RECUSADO -> orderUseCase.updateStatus(persistedPayment.getOrder().getId(), OrderStatus.CANCELADO);
        }
    }

    private void validateUpdateStatus(PaymentStatus newStatus, PaymentStatus oldStatus) {
        if (!canStatusBeUpdatedFrom(oldStatus)) {
            throw new InvalidAttributeException(String.format("You can not change status from payments with status %s.", oldStatus.name()), "oldStatus");
        }
        if (!canStatusUpdateTo(newStatus)) {
            throw new InvalidAttributeException(String.format("You can not change payments status to %s.", newStatus.name()), "new Status");
        }
    }

    private boolean canStatusBeUpdatedFrom(PaymentStatus status) {
        List<PaymentStatus> statusThatCanBeUpdatedFrom = List.of(PaymentStatus.ABERTO);
        return statusThatCanBeUpdatedFrom.contains(status);
    }

    private boolean canStatusUpdateTo(PaymentStatus status) {
        List<PaymentStatus> statusThatCanBeUpdatedTo = List.of(PaymentStatus.APROVADO, PaymentStatus.RECUSADO);
        return statusThatCanBeUpdatedTo.contains(status);
    }
}
