package ru.finess.finess.payment.domain;

public enum PaymentStatus {
  INITIALIZED,
  RECIEVING,
  RECIEVED,
  SENDING,
  SENT,
  FAILED,
  CANCELED,
}
