package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.application.UserRepository;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@Service
@RequiredArgsConstructor
public class InitializePaymentUseCase
    implements UseCase<
        PaymentInitialization,
        InitializePaymentUseCase.Errors,
        InitializePaymentUseCase.Parameters> {

  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PaymentInitializationService initializationService;

  public sealed interface Errors permits Errors.UserNotFound, Errors.AccountNotFound {

    record UserNotFound(UserId userId) implements Errors {}

    record AccountNotFound(PaymentQrCodeId qrCodeId) implements Errors {}
  }

  public record Parameters(
      @NonNull UserId userId,
      @NonNull AccountId accountId,
      @NonNull PaymentAmount amount,
      @NonNull OffsetDateTime now) {}

  @Override
  public Result<PaymentInitialization, Errors> execute(@NonNull Parameters parameters) {
    AccountId accountId = parameters.accountId();
    if (!accountRepository.existsById(accountId)) {
      return Result.error(new Errors.UserNotFound(parameters.userId()));
    }

    UserId userId = parameters.userId();
    if (!userRepository.existsById(userId)) {
      return Result.error(new Errors.UserNotFound(userId));
    }

    PaymentInitialization initialization =
        PaymentInitialization.of(parameters.now(), userId, accountId, parameters.amount());
    initializationService.enqueue(initialization);
    return Result.success(initialization);
  }
}
