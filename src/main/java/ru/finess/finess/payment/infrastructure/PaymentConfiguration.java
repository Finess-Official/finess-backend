package ru.finess.finess.payment.infrastructure;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PaymentConfiguration {

  private final int corePoolSize;
  private final int maxPoolSize;
  private final int queueCapacity;

  public PaymentConfiguration(
      @Value("${finess.payment.initialization.executor.corePoolSize}") int corePoolSize,
      @Value("${finess.payment.initialization.executor.maxPoolSize}") int maxPoolSize,
      @Value("${finess.payment.initialization.executor.queueCapacity}") int queueCapacity) {
    this.corePoolSize = corePoolSize;
    this.maxPoolSize = maxPoolSize;
    this.queueCapacity = queueCapacity;
  }

  @Bean(name = "paymentInitializationExecutor")
  public Executor paymentInitializationExecutor() {
    ThreadPoolTaskExecutor taskExecutor =
        new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(corePoolSize)
            .maxPoolSize(maxPoolSize)
            .queueCapacity(queueCapacity)
            .threadNamePrefix("PaymentInitialization")
            .build();
    taskExecutor.initialize();
    return taskExecutor;
  }
}
