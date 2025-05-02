package ru.finess.finess.payment.infrastructure;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.utils.StringUtils;
import ru.finess.finess.tbank.infrastructure.dto.InitFULLDto;

@Component
public class TBankRequestTokenBuilder {

  private static final Map<String, Function<InitFULLDto, String>> TOKEN_ATTRIBUTES_FROM_DTO =
      Map.ofEntries(
          Map.entry("TerminalKey", InitFULLDto::getTerminalKey),
          Map.entry("Amount", dto -> String.valueOf(dto.getAmount())),
          Map.entry("OrderId", InitFULLDto::getOrderId),
          Map.entry("Description", InitFULLDto::getDescription),
          Map.entry("CustomerKey", InitFULLDto::getCustomerKey),
          Map.entry("Recurrent", InitFULLDto::getRecurrent),
          Map.entry("PayType", dto -> StringUtils.toStringOrNull(dto.getPayType())),
          Map.entry("Language", InitFULLDto::getLanguage),
          Map.entry("NotificationURL", dto -> StringUtils.toStringOrNull(dto.getNotificationURL())),
          Map.entry("SuccessURL", dto -> StringUtils.toStringOrNull(dto.getSuccessURL())),
          Map.entry("FailURL", dto -> StringUtils.toStringOrNull(dto.getFailURL())),
          Map.entry("RedirectDueDate", dto -> StringUtils.toStringOrNull(dto.getRedirectDueDate())),
          Map.entry("Descriptor", InitFULLDto::getDescriptor));

  private final Map.Entry<String, String> terminalPassword;

  TBankRequestTokenBuilder(
      @Value("${payment.acquiring.terminal.password}") String terminalPassword) {
    this.terminalPassword = Map.entry("Password", terminalPassword);
  }

  public String buildToken(@NonNull InitFULLDto dto) {
    Stream<Map.Entry<String, String>> tokenAttributesStream =
        TOKEN_ATTRIBUTES_FROM_DTO.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue().apply(dto)))
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().apply(dto)));

    String joinedValues =
        Stream.concat(tokenAttributesStream, Stream.of(terminalPassword))
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .collect(Collectors.joining());

    return StringUtils.sha256Digest(joinedValues);
  }
}
