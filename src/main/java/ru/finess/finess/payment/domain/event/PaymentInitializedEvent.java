package ru.finess.finess.payment.domain.event;

import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentInitializationId;

public record PaymentInitializedEvent(@NonNull PaymentInitializationId paymentInitializationId) {}
