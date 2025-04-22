package ru.finess.finess.payment.application;

import static ru.finess.finess.payment.application.PaymentQrCodeCreationUseCase.*;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@Service
@RequiredArgsConstructor
public class PaymentQrCodeCreationUseCase
    implements UseCase<PaymentQrCode, AccountNotFound, Parameters> {

  private final AccountRepository accountRepository;
  private final QrCodeIdGenerator qrCodeIdGenerator;

  public record Parameters(
      @NonNull UserId currentUser, @NonNull AccountId accountId, @NonNull PaymentAmount amount) {}

  public record AccountNotFound(AccountId accountId) {}

  @Transactional
  @Override
  public Result<PaymentQrCode, AccountNotFound> execute(@NonNull Parameters parameters) {
    if (isAccountExists(parameters)) {
      PaymentQrCodeId qrCodeId = qrCodeIdGenerator.generate();
      PaymentQrCode qrCode =
          PaymentQrCode.of(qrCodeId, parameters.accountId(), parameters.amount());
      return Result.success(qrCode);
    } else {
      return Result.error(new AccountNotFound(parameters.accountId()));
    }
  }

  private boolean isAccountExists(Parameters parameters) {
    return accountRepository.existsForUser(parameters.currentUser(), parameters.accountId());
  }
}
