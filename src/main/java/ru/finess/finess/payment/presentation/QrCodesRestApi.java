package ru.finess.finess.payment.presentation;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.GettingQrCodeByIdUseCase;
import ru.finess.finess.payment.application.PaymentQrCodeCreationUseCase;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentQrCodeId;
import ru.finess.finess.payment.presentation.api.QrcodesApi;
import ru.finess.finess.payment.presentation.dto.QRCodeCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.QRCodeDto;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class QrCodesRestApi implements QrcodesApi {

  private final PaymentQrCodeCreationUseCase qrCodeCreationUseCase;
  private final GettingQrCodeByIdUseCase gettingQrCodeByIdUseCase;
  private final Supplier<Optional<UserId>> currentUserSupplier;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<QRCodeDto> createQRCode(
      QRCodeCreationParametersDto qrCodeCreationParametersDto) {
    PaymentQrCodeCreationUseCase.Parameters parameters =
        createParameters(qrCodeCreationParametersDto);

    return qrCodeCreationUseCase
        .execute(parameters)
        .mapError(accountNotFound -> HttpStatus.NOT_FOUND)
        .map(qrCode -> conversionService.convert(qrCode, QRCodeDto.class))
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  @Override
  public ResponseEntity<QRCodeDto> getQRCode(String id) {
    PaymentQrCodeId paymentQrCodeId = new PaymentQrCodeId(id);
    GettingQrCodeByIdUseCase.Parameters parameters =
        new GettingQrCodeByIdUseCase.Parameters(paymentQrCodeId);

    return gettingQrCodeByIdUseCase
        .execute(parameters)
        .map(qrCode -> conversionService.convert(qrCode, QRCodeDto.class))
        .map(ResponseEntity::ok)
        .recoverError(error -> ResponseEntity.notFound().build());
  }

  private PaymentQrCodeCreationUseCase.Parameters createParameters(
      QRCodeCreationParametersDto qrCodeCreationParametersDto) {
    AccountId accountId = new AccountId(qrCodeCreationParametersDto.getAccountId());
    PaymentAmount paymentAmount =
        new PaymentAmount(BigDecimal.valueOf(qrCodeCreationParametersDto.getAmount()));

    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not present"));
    return new PaymentQrCodeCreationUseCase.Parameters(currentUser, accountId, paymentAmount);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  private void handleIllegalArgumentException(IllegalArgumentException e) {
    log.error("Failed to process request in QrCodesRestApi: {}", e.getMessage());
  }
}
