package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;

@Service
@RequiredArgsConstructor
public class GettingPaymentInitializationUseCase
    implements UseCase<
        PaymentInitialization,
        GettingPaymentInitializationUseCase.NotFound,
        GettingPaymentInitializationUseCase.Parameters> {

  public record NotFound(PaymentInitializationId paymentInitializationId) {}

  public record Parameters(@NonNull UserId currentUser, @NonNull PaymentInitializationId id) {}

  private final PaymentInitializationRepository paymentInitializationRepository;

  @Override
  public Result<PaymentInitialization, NotFound> execute(@NonNull Parameters parameters) {
    PaymentInitializationId id = parameters.id;
    Optional<PaymentInitialization> optionalPaymentInitialization =
        paymentInitializationRepository.findForUser(parameters.currentUser, id);
    return Result.fromOptional(optionalPaymentInitialization, new NotFound(id));
  }
}
