package ru.finess.finess.payment.infrastructure;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;
import ru.finess.finess.payment.application.QrCodeIdGenerator;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@Component
public class HashidQrCodeIdGenerator implements QrCodeIdGenerator {

  private final Sqids generator;
  private final JpaPaymentQrCodeRepository repository;

  public HashidQrCodeIdGenerator(
      @Value("${payment.qrcode.alphabet}") String alphabet, JpaPaymentQrCodeRepository repository) {
    this.generator = Sqids.builder().alphabet(alphabet).minLength(6).build();
    this.repository = repository;
  }

  @Override
  public PaymentQrCodeId generate() {
    long sequenceNumber = repository.getNextSequenceNumber();
    String hashid = generator.encode(List.of(sequenceNumber));
    return new PaymentQrCodeId(hashid);
  }
}
