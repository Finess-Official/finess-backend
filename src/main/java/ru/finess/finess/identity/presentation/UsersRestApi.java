package ru.finess.finess.identity.presentation;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.application.UserRepository;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.presentation.api.UsersApi;
import ru.finess.finess.identity.presentation.dto.UserDto;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class UsersRestApi implements UsersApi {

  private final Supplier<Optional<UserId>> currentUserSupplier;
  private final UserRepository userRepository;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<UserDto> getCurrentUser() {
    return currentUserSupplier
        .get()
        .flatMap(userRepository::findById)
        .map(user -> conversionService.convert(user, UserDto.class))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build());
  }
}
