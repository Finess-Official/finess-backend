package ru.finess.finess.payment.infrastructure;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.finess.finess.payment.application.InternetAcquiringService;
import ru.finess.finess.payment.domain.*;
import ru.finess.finess.tbank.infrastructure.api.V2ApiClient;
import ru.finess.finess.tbank.infrastructure.dto.GetState200ResponseDto;
import ru.finess.finess.tbank.infrastructure.dto.GetStateFULLDto;
import ru.finess.finess.tbank.infrastructure.dto.InitFULLDto;
import ru.finess.finess.tbank.infrastructure.dto.ResponseDto;

@Slf4j
@Service
public class TBankApiService implements InternetAcquiringService {

  private final String terminalKey;
  private final TBankRequestTokenBuilder tBankRequestTokenBuilder;
  private final V2ApiClient tbankApiClient; // todo: move service to infrastructure

  public TBankApiService(
      @Value("${payment.acquiring.terminal.key}") String terminalKey,
      TBankRequestTokenBuilder tBankRequestTokenBuilder,
      V2ApiClient tbankApiClient) {
    this.terminalKey = terminalKey;
    this.tBankRequestTokenBuilder = tBankRequestTokenBuilder;
    this.tbankApiClient = tbankApiClient;
  }

  @Override
  @Async(value = "paymentInitializationExecutor")
  public CompletableFuture<AcquiringPayment> initializePayment(
      @NonNull PaymentAmount paymentAmount, @NonNull PaymentInitializationId id) {

    InitFULLDto dto = createInitializationDto(paymentAmount, id);
    ResponseEntity<ResponseDto> response = tbankApiClient.init(dto);

    if (response.getStatusCode() != HttpStatus.OK) {
      log.error("Failed to initialize payment for initialization {}. Response: {}", id, response);
      return CompletableFuture.failedFuture(
          new IllegalStateException("Failed to initialize payment. Api response: " + response));
    }

    return Optional.ofNullable(response.getBody())
        .filter(ResponseDto::getSuccess)
        .flatMap(this::toAcquiringPayment)
        .map(CompletableFuture::completedFuture)
        .orElseGet(
            () ->
                CompletableFuture.failedFuture(
                    new IllegalStateException(
                        "Failed to parse initialization response body. Api response: "
                            + response.getBody())));
  }

  @Async(value = "paymentInitializationExecutor")
  @Override
  public CompletableFuture<PaymentStatus> getPaymentStatus(
      @NonNull AcquiringPaymentId acquiringPaymentId) {
    GetStateFULLDto dto = createGettingStateDto(acquiringPaymentId);

    ResponseEntity<GetState200ResponseDto> response = tbankApiClient.getState(dto);
    if (response.getStatusCode() != HttpStatus.OK) {
      log.error("Failed to get state for payment {}. Response: {}", acquiringPaymentId, response);
      return CompletableFuture.failedFuture(
          new IllegalStateException("Failed to get state for payment " + acquiringPaymentId));
    }

    return Optional.ofNullable(response.getBody())
        .filter(stateResponse -> Objects.nonNull(stateResponse.getStatus()))
        .map(this::convertAcquiringStateToInternalStatus)
        .map(CompletableFuture::completedFuture)
        .orElseGet(
            () ->
                CompletableFuture.failedFuture(
                    new IllegalStateException(
                        "Failed to get state for payment " + acquiringPaymentId)));
  }

  private InitFULLDto createInitializationDto(
      PaymentAmount paymentAmount, PaymentInitializationId id) {
    InitFULLDto dto =
        new InitFULLDto()
            .terminalKey(terminalKey)
            .amount(paymentAmount.value())
            .orderId(id.value().toString());

    String token = tBankRequestTokenBuilder.buildToken(dto);
    dto.setToken(token);
    return dto;
  }

  private Optional<AcquiringPayment> toAcquiringPayment(ResponseDto responseDto) {
    if (Objects.nonNull(responseDto.getPaymentId())
        && Objects.nonNull(responseDto.getPaymentURL())) {
      AcquiringPaymentId paymentId = new AcquiringPaymentId(responseDto.getPaymentId());
      AcquiringPayment acquiringPayment =
          new AcquiringPayment(paymentId, responseDto.getPaymentURL());
      return Optional.of(acquiringPayment);
    }
    return Optional.empty();
  }

  private GetStateFULLDto createGettingStateDto(@NonNull AcquiringPaymentId acquiringPaymentId) {
    GetStateFULLDto dto =
        new GetStateFULLDto().terminalKey(terminalKey).paymentId(acquiringPaymentId.value());
    String token = tBankRequestTokenBuilder.buildToken(dto);
    return dto.token(token);
  }

  private PaymentStatus convertAcquiringStateToInternalStatus(
      GetState200ResponseDto stateResponse) {
    return switch (stateResponse.getStatus()) {
      case NEW, FORM_SHOWED, AUTHORIZING, _3_DS_CHECKING, _3_DS_CHECKED, AUTHORIZED, CONFIRMING ->
          PaymentStatus.RECIEVING;
      case CONFIRMED -> PaymentStatus.RECIEVED;
      case REVERSING, PARTIAL_REVERSED, REVERSED, CANCELED -> PaymentStatus.CANCELED;
      case DEADLINE_EXPIRED, AUTH_FAIL, REJECTED -> PaymentStatus.FAILED;
      case REFUNDING, PARTIAL_REFUNDED, REFUNDED ->
          throw new UnsupportedOperationException("Not supported yet.");
    };
  }
}
