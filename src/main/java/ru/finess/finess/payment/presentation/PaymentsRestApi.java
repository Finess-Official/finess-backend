package ru.finess.finess.payment.presentation;

import static ru.finess.finess.payment.application.InitializePaymentUseCase.*;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.InitializePaymentUseCase;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentQrCodeId;
import ru.finess.finess.payment.presentation.api.PaymentsApi;
import ru.finess.finess.payment.presentation.dto.PaymentCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.PaymentInitializationTaskDto;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentsRestApi implements PaymentsApi {

  private final Supplier<Optional<UserId>> currentUserSupplier;
  private final InitializePaymentUseCase initializePaymentUseCase;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<PaymentInitializationTaskDto> createPaymentInitializationTask(
      PaymentCreationParametersDto paymentCreationParametersDto) {
    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not authenticated"));

    return initializePayment(paymentCreationParametersDto, currentUser)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  private Result<PaymentInitialization, HttpStatus> initializePayment(
      PaymentCreationParametersDto paymentCreationParametersDto, UserId currentUser) {
    Parameters parameters =
        new Parameters(
            currentUser,
            new PaymentQrCodeId(paymentCreationParametersDto.getQrCodeId()),
            OffsetDateTime.now());
    return initializePaymentUseCase
        .execute(parameters)
        .mapError(
            error ->
                switch (error) {
                  case Errors.QrCodeNotFound ignored -> HttpStatus.NOT_FOUND;
                  case Errors.UserNotFound ignored -> HttpStatus.UNAUTHORIZED;
                });
  }

  private PaymentInitializationTaskDto toDto(PaymentInitialization initialization) {
    return conversionService.convert(initialization, PaymentInitializationTaskDto.class);
  }
}
