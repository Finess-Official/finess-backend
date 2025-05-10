package ru.finess.finess.payment.application;

import java.util.Optional;
import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

public interface PaymentQrCodeRepository {

  void save(@NonNull PaymentQrCode paymentQrCode);

  Optional<PaymentQrCode> findById(@NonNull PaymentQrCodeId paymentQrCodeId);
}
