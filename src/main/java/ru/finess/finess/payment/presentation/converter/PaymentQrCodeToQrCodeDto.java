package ru.finess.finess.payment.presentation.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.presentation.dto.QRCodeDto;

@Component
public class PaymentQrCodeToQrCodeDto implements Converter<PaymentQrCode, QRCodeDto> {
  @Override
  public QRCodeDto convert(@NonNull PaymentQrCode source) {
    return new QRCodeDto()
        .id(source.id().value())
        .accountId(source.accountId().id())
        .amount(source.amount().value().floatValue());
  }
}
