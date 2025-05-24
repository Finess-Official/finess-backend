package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.Payment_;

@Component
@RequiredArgsConstructor
public class GettingPaymentsUseCase
    implements UseCase<List<Payment>, Void, GettingPaymentsUseCase.Parameters> {

  public record Parameters(Specification<Payment> specification) {}

  private static final Sort DEFAULT_SORT = Sort.by(Payment_.CREATED_AT).descending();
  private final PaymentRepository paymentRepository;

  @Override
  public Result<List<Payment>, Void> execute(@NonNull Parameters parameters) {
    Specification<Payment> specification = parameters.specification;
    return Result.success(paymentRepository.findAll(specification, DEFAULT_SORT));
  }
}
