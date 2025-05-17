package ru.finess.finess.payment.domain;

import java.net.URI;
import lombok.NonNull;

public record AcquiringPayment(@NonNull AcquiringPaymentId id, @NonNull URI paymentLink) {}
