package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@Service
@RequiredArgsConstructor
public class GettingQrCodeByIdUseCase
    implements UseCase<
        PaymentQrCode,
        GettingQrCodeByIdUseCase.QrCodeNotFound,
        GettingQrCodeByIdUseCase.Parameters> {

  private final PaymentQrCodeRepository paymentQrCodeRepository;

  public record Parameters(@NonNull PaymentQrCodeId qrCodeId) {}

  public record QrCodeNotFound(PaymentQrCodeId qrCodeId) {}

  @Transactional(readOnly = true)
  @Override
  public Result<PaymentQrCode, QrCodeNotFound> execute(@NonNull Parameters parameters) {
    return Result.fromOptional(
        paymentQrCodeRepository.findById(parameters.qrCodeId()),
        new QrCodeNotFound(parameters.qrCodeId()));
  }
}
