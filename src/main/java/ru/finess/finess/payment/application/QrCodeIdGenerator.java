package ru.finess.finess.payment.application;

import ru.finess.finess.payment.domain.PaymentQrCodeId;

public interface QrCodeIdGenerator {

  PaymentQrCodeId generate();
}
