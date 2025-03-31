package ru.finess.finess.common.application;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionWrapper {

  @Transactional
  public <T> T execute(Supplier<T> operation) {
    try {
      return operation.get();
    } catch (Exception e) {
      throw new RuntimeException("Transaction failed", e);
    }
  }
}
