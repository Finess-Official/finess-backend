package ru.finess.finess.payment.presentation.converter;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserFullName;
import ru.finess.finess.identity.infrastructure.JpaUserRepository;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.infrastructure.JpaAccountRepository;
import ru.finess.finess.payment.presentation.dto.AccountDto;
import ru.finess.finess.payment.presentation.dto.PaymentDto;
import ru.finess.finess.payment.presentation.dto.PaymentRecipientDto;
import ru.finess.finess.payment.presentation.dto.UserDto;

@Component
@RequiredArgsConstructor
public class PaymentToPaymentDtoConverter implements Converter<Payment, PaymentDto> {

  // TODO: Remove this dependency to avoid n+1 select problem
  private final JpaUserRepository userRepository;
  private final AccountToAccountDto accountToAccountDto;
  private final JpaAccountRepository accountRepository;

  @Override
  public PaymentDto convert(@NonNull Payment source) {
    return new PaymentDto()
        .id(source.id().id())
        .status(convertStatus(source))
        .amount(source.amount().value().floatValue())
        .createdAt(source.createdAt())
        .sender(convertUser(source))
        .recipient(convertRecipient(source));
  }

  private PaymentDto.StatusEnum convertStatus(@NonNull Payment source) {
    return switch (source.status()) {
      case RECIEVED -> PaymentDto.StatusEnum.COMPLETED;
      case FAILED -> PaymentDto.StatusEnum.FAILED;
      case INITIALIZED, RECIEVING, SENDING, SENT, CANCELED ->
          throw new UnsupportedOperationException(
              "Status is not supported" + " for PaymentDto conversion: " + source.status());
    };
  }

  private UserDto convertUser(@NonNull Payment source) {
    if (source.initiator() == null) return null;

    return userRepository
        .findById(source.initiator())
        .map(PaymentToPaymentDtoConverter::convertUser)
        .orElse(null);
  }

  private static UserDto convertUser(User user) {
    UserFullName fullName = user.fullName();
    return new UserDto()
        .id(user.id().value())
        .firstName(fullName.firstName())
        .lastName(fullName.lastName())
        .middleName(fullName.middleName());
  }

  private PaymentRecipientDto convertRecipient(@NonNull Payment source) {
    AccountId accountId = source.recipientAccount();
    Optional<User> optionalUser = userRepository.findWithAccount(accountId);
    if (optionalUser.isPresent()) {
      User user = optionalUser.get();
      return new PaymentRecipientDto()
          .profile(convertUser(user))
          .account(convertAccount(accountId));
    }
    throw new IllegalStateException("Recipient account not found: " + accountId);
  }

  private AccountDto convertAccount(AccountId accountId) {
    Account account =
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountId));
    return accountToAccountDto.convert(account);
  }
}
