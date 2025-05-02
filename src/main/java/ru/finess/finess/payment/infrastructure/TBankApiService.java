package ru.finess.finess.payment.infrastructure;

import java.net.URI;
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
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentInitializationId;
import ru.finess.finess.tbank.infrastructure.api.V2ApiClient;
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
  public CompletableFuture<URI> initializePayment(
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
        .flatMap(responseDto -> Optional.ofNullable(responseDto.getPaymentURL()))
        .map(CompletableFuture::completedFuture)
        .orElseGet(
            () ->
                CompletableFuture.failedFuture(
                    new IllegalStateException(
                        "Failed to parse initialization response body. Api response: "
                            + response.getBody())));
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
}
