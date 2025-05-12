package ru.finess.finess.payment.presentation.converter;

import java.net.URI;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationStatus;
import ru.finess.finess.payment.presentation.dto.PaymentInitializationTaskDto;

@Component
public class PaymentInitializationToPaymentInitializationTaskDto
    implements Converter<PaymentInitialization, PaymentInitializationTaskDto> {

  @Override
  public PaymentInitializationTaskDto convert(@NonNull PaymentInitialization source) {
    return new PaymentInitializationTaskDto()
        .id(source.id().value())
        .status(convert(source.status()))
        .acquiringPaymentUrl(
            source.acquiringPaymentUrl() == null ? null : URI.create(source.acquiringPaymentUrl()))
        .createdAt(source.createdAt());
  }

  private PaymentInitializationTaskDto.StatusEnum convert(PaymentInitializationStatus status) {
    return switch (status) {
      case NEW, IN_PROGRESS -> PaymentInitializationTaskDto.StatusEnum.IN_PROGRESS;
      case INITIALIZED -> PaymentInitializationTaskDto.StatusEnum.INITIALIZED;
      case FAILED -> PaymentInitializationTaskDto.StatusEnum.FAILED;
    };
  }
}
