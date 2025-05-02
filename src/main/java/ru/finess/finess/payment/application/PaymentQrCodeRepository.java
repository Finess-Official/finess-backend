package ru.finess.finess.payment.application;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

public interface PaymentQrCodeRepository {

  void save(@NonNull PaymentQrCode paymentQrCode);

  Optional<PaymentQrCode> findById(@NonNull PaymentQrCodeId paymentQrCodeId);

  boolean existsById(@NonNull PaymentQrCodeId qrCodeId);

  List<PaymentQrCode> findAllById(@NonNull Iterable<PaymentQrCodeId> ids);
}
