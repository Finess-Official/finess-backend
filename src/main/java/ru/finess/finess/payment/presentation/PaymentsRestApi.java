package ru.finess.finess.payment.presentation;

import static ru.finess.finess.payment.application.InitializePaymentUseCase.*;

import com.github.sviperll.result4j.Result;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.GettingPaymentBeaconByIdUseCase;
import ru.finess.finess.payment.application.GettingPaymentInitializationUseCase;
import ru.finess.finess.payment.application.GettingPaymentsUseCase;
import ru.finess.finess.payment.application.GettingQrCodeByIdUseCase;
import ru.finess.finess.payment.application.InitializePaymentUseCase;
import ru.finess.finess.payment.application.PaymentRepository;
import ru.finess.finess.payment.application.PaymentSpecifications;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.domain.PaymentBeaconId;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;
import ru.finess.finess.payment.domain.PaymentStatus;
import ru.finess.finess.payment.presentation.api.PaymentsApi;
import ru.finess.finess.payment.presentation.dto.AssociationIdDto;
import ru.finess.finess.payment.presentation.dto.BeaconAssociationIdDto;
import ru.finess.finess.payment.presentation.dto.PaymentCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.PaymentDto;
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
  private final GettingPaymentBeaconByIdUseCase gettingBeaconByIdUseCase;
  private final GettingPaymentsUseCase gettingPaymentsUseCase;
  private final ConversionService conversionService;
  private final PaymentRepository paymentRepository;

  @Override
  public ResponseEntity<PaymentInitializationTaskDto> createPaymentInitializationTask(
      PaymentCreationParametersDto parameters) {
    UserId currentUser = getCurrentUser();

    AssociationIdDto associationId = parameters.getAssociationId();
    Result<Map.Entry<AccountId, PaymentAmount>, HttpStatus> paymentInfoResult =
        switch (associationId) {
          case BeaconAssociationIdDto dto ->
              findBeaconById(dto).map(beacon -> Map.entry(beacon.accountId(), beacon.amount()));
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
    UserId currentUser = getCurrentUser();

    GettingPaymentInitializationUseCase.Parameters parameters =
        new GettingPaymentInitializationUseCase.Parameters(
            currentUser, new PaymentInitializationId(id));

    return gettingPaymentInitializationUseCase
        .execute(parameters)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<PaymentDto>> getPayments(
      OffsetDateTime startDate,
      OffsetDateTime endDate,
      Float minAmount,
      Float maxAmount,
      String status) {

    UserId currentUser = getCurrentUser();
    PaymentSpecifications.Builder builder = PaymentSpecifications.builder(currentUser);

    Optional.ofNullable(startDate).ifPresent(builder::fromDate);

    Optional.ofNullable(endDate).ifPresent(builder::toDate);

    Optional.ofNullable(minAmount)
        .map(BigDecimal::valueOf)
        .map(PaymentAmount::new)
        .ifPresent(builder::fromAmount);

    Optional.ofNullable(maxAmount)
        .map(BigDecimal::valueOf)
        .map(PaymentAmount::new)
        .ifPresent(builder::toAmount);

    Optional.ofNullable(status).map(this::toStatus).ifPresent(builder::status);

    Specification<Payment> specification = builder.build();

    return gettingPaymentsUseCase
        .execute(new GettingPaymentsUseCase.Parameters(specification))
        .map(this::toListDto)
        .map(ResponseEntity::ok)
        .recoverError(ignored -> ResponseEntity.internalServerError().build());
  }

  private UserId getCurrentUser() {
    return currentUserSupplier
        .get()
        .orElseThrow(() -> new SecurityException("Current user is not authenticated"));
  }

  private Result<PaymentBeacon, HttpStatus> findBeaconById(BeaconAssociationIdDto dto) {
    return gettingBeaconByIdUseCase
        .execute(new PaymentBeaconId(dto.getBeaconId()))
        .mapError(notFound -> HttpStatus.NOT_FOUND);
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

  private List<PaymentDto> toListDto(List<Payment> payments) {
    return payments.stream()
        .map(payment -> conversionService.convert(payment, PaymentDto.class))
        .toList();
  }

  private PaymentStatus toStatus(String status) {
    return Optional.ofNullable(status)
        .flatMap(
            paymentStatus ->
                switch (paymentStatus) {
                  case "FAILED" -> Optional.of(PaymentStatus.FAILED);
                  case "COMPLETED" -> Optional.of(PaymentStatus.RECIEVED);
                  default -> Optional.empty();
                })
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment status: " + status));
  }
}
