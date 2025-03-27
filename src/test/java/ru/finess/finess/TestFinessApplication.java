package ru.finess.finess;

import org.springframework.boot.SpringApplication;

public class TestFinessApplication {

  public static void main(String[] args) {
    SpringApplication.from(FinessApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
