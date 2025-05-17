package ru.finess.finess.payment.presentation.converter;

import java.net.URI;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.presentation.dto.PaymentInitializationTaskDto;

@Component
public class PaymentInitializationToPaymentInitializationTaskDto
    implements Converter<PaymentInitialization, PaymentInitializationTaskDto> {

  private final int retryLimit;

  public PaymentInitializationToPaymentInitializationTaskDto(
      @Value("${finess.payment.initialization.retryLimit}") int retryLimit) {
    this.retryLimit = retryLimit;
  }

  @Override
  public PaymentInitializationTaskDto convert(@NonNull PaymentInitialization source) {
    return new PaymentInitializationTaskDto()
        .id(source.id().value())
        .status(convertStatus(source))
        .acquiringPaymentUrl(
            source.acquiringPaymentUrl() == null ? null : URI.create(source.acquiringPaymentUrl()))
        .createdAt(source.createdAt());
  }

  private PaymentInitializationTaskDto.StatusEnum convertStatus(
      PaymentInitialization initialization) {
    return switch (initialization.status()) {
      case NEW, IN_PROGRESS -> PaymentInitializationTaskDto.StatusEnum.IN_PROGRESS;
      case INITIALIZED -> PaymentInitializationTaskDto.StatusEnum.INITIALIZED;
      case FAILED ->
          initialization.canProcess(retryLimit)
              ? PaymentInitializationTaskDto.StatusEnum.IN_PROGRESS
              : PaymentInitializationTaskDto.StatusEnum.FAILED;
    };
  }
}
