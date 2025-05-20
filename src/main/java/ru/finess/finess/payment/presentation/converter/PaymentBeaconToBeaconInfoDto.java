package ru.finess.finess.payment.presentation.converter;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.AccountRepository;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.presentation.dto.BeaconInfoDto;

@Component
@RequiredArgsConstructor
public class PaymentBeaconToBeaconInfoDto implements Converter<PaymentBeacon, BeaconInfoDto> {

  private final AccountRepository accountRepository;
  private final AccountToAccountDto accountToAccountDto;

  @Override
  public BeaconInfoDto convert(PaymentBeacon source) {
    Account account = accountRepository.findById(source.accountId()).orElseThrow();
    return new BeaconInfoDto()
        .id(source.id().value())
        .account(accountToAccountDto.convert(account))
        .bluetoothId(UUID.fromString(source.bluetoothId().value()))
        .major(source.major())
        .minor(source.minor());
  }
}
