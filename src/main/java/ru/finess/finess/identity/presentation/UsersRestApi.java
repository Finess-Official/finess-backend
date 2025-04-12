package ru.finess.finess.identity.presentation;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.presentation.api.UsersApi;
import ru.finess.finess.identity.presentation.dto.UserDto;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class UsersRestApi implements UsersApi {

  private final Supplier<Optional<User>> currentUserSupplier;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<UserDto> getCurrentUser() {
    return currentUserSupplier
        .get()
        .map(user -> conversionService.convert(user, UserDto.class))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build());
  }
}
