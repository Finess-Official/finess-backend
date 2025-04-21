package ru.finess.finess.payment.presentation.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.presentation.dto.AccountDto;

@Component
public class AccountToAccountDto implements Converter<Account, AccountDto> {

  @Override
  public AccountDto convert(@NonNull Account source) {
    return new AccountDto()
        .id(source.id().id())
        .accountNumber(source.number().value())
        .bik(source.bik().value())
        .inn(source.inn().value())
        .ownerName(source.ownerName().value());
  }
}
