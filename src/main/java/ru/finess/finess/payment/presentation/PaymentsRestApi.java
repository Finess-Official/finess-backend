package ru.finess.finess.payment.presentation;

import static ru.finess.finess.payment.application.InitializePaymentUseCase.*;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.GettingPaymentInitializationUseCase;
import ru.finess.finess.payment.application.GettingQrCodeByIdUseCase;
import ru.finess.finess.payment.application.InitializePaymentUseCase;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;
import ru.finess.finess.payment.presentation.api.PaymentsApi;
import ru.finess.finess.payment.presentation.dto.AssociationIdDto;
import ru.finess.finess.payment.presentation.dto.BeaconAssociationIdDto;
import ru.finess.finess.payment.presentation.dto.PaymentCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.PaymentInitializationTaskDto;
import ru.finess.finess.payment.presentation.dto.QrCodeAssociationIdDto;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentsRestApi implements PaymentsApi {

  private final Supplier<Optional<UserId>> currentUserSupplier;
  private final InitializePaymentUseCase initializePaymentUseCase;
  private final GettingPaymentInitializationUseCase gettingPaymentInitializationUseCase;
  private final GettingQrCodeByIdUseCase gettingQrCodeByIdUseCase;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<PaymentInitializationTaskDto> createPaymentInitializationTask(
      PaymentCreationParametersDto parameters) {
    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not authenticated"));

    AssociationIdDto associationId = parameters.getAssociationId();
    Result<Map.Entry<AccountId, PaymentAmount>, HttpStatus> paymentInfoResult =
        switch (associationId) {
          case BeaconAssociationIdDto ignored ->
              throw new UnsupportedOperationException("Not supported yet");
          case QrCodeAssociationIdDto dto ->
              findQrCodeId(dto.getQrCodeId())
                  .map(qrCode -> Map.entry(qrCode.accountId(), qrCode.amount()));
          default -> throw new IllegalStateException("Unexpected value: " + associationId);
        };

    return paymentInfoResult
        .flatMap(paymentInfo -> initializePayment(paymentInfo, currentUser))
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  @Override
  public ResponseEntity<PaymentInitializationTaskDto> getPaymentInitializationTask(UUID id) {
    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not authenticated"));

    GettingPaymentInitializationUseCase.Parameters parameters =
        new GettingPaymentInitializationUseCase.Parameters(
            currentUser, new PaymentInitializationId(id));

    return gettingPaymentInitializationUseCase
        .execute(parameters)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.notFound().build());
  }

  private Result<PaymentQrCode, HttpStatus> findQrCodeId(String qrCodeId) {
    PaymentQrCodeId qrCodeId1 = new PaymentQrCodeId(qrCodeId);
    GettingQrCodeByIdUseCase.Parameters parameters =
        new GettingQrCodeByIdUseCase.Parameters(qrCodeId1);
    return gettingQrCodeByIdUseCase.execute(parameters).mapError(notFound -> HttpStatus.NOT_FOUND);
  }

  private Result<PaymentInitialization, HttpStatus> initializePayment(
      Map.Entry<AccountId, PaymentAmount> accountAmount, UserId currentUser) {
    Parameters parameters =
        new Parameters(
            currentUser, accountAmount.getKey(), accountAmount.getValue(), OffsetDateTime.now());
    return initializePaymentUseCase
        .execute(parameters)
        .mapError(
            error ->
                switch (error) {
                  case Errors.AccountNotFound ignored -> HttpStatus.NOT_FOUND;
                  case Errors.UserNotFound ignored -> HttpStatus.UNAUTHORIZED;
                });
  }

  private PaymentInitializationTaskDto toDto(PaymentInitialization initialization) {
    return conversionService.convert(initialization, PaymentInitializationTaskDto.class);
  }
}
