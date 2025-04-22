package ru.finess.finess.payment.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.payment.application.QrCodeIdGenerator;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@IntegrationTest
class JpaPaymentQrCodeRepositoryIT {

  @Autowired JpaPaymentQrCodeRepository sut;

  @Autowired QrCodeIdGenerator generator;

  @DisplayName("Check saving qr code")
  @Test
  void testSaving() {
    // Arrange
    PaymentQrCodeId qrCodeId = generator.generate();
    PaymentQrCode qrCode =
        PaymentQrCode.of(qrCodeId, AccountId.random(), new PaymentAmount(BigDecimal.ONE));

    // Act
    sut.save(qrCode);

    // Assert
    assertTrue(sut.findById(qrCodeId).isPresent());
  }

  @DisplayName("Check loading qr code")
  @Test
  void testLoading() {
    // Arrange
    PaymentQrCodeId qrCodeId = generator.generate();
    PaymentQrCode qrCode =
        PaymentQrCode.of(qrCodeId, AccountId.random(), new PaymentAmount(BigDecimal.ONE));
    sut.save(qrCode);

    // Act
    PaymentQrCode actual = sut.findById(qrCodeId).orElseThrow();

    // Assert
    assertEquals(qrCode, actual);
  }
}
