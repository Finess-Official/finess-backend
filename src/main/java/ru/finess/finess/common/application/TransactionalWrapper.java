package ru.finess.finess.common.application;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionalWrapper {

  @Transactional
  public <T> T runInTransaction(Supplier<T> runnable) {
    return runnable.get();
  }
}
