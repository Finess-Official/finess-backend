package ru.finess.finess.identity.presentation.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserFullName;
import ru.finess.finess.identity.presentation.dto.UserDto;

@Component
public class UserToUserDtoConverter implements Converter<User, UserDto> {

  @Override
  public UserDto convert(User source) {
    UserFullName fullName = source.fullName();
    return new UserDto()
        .id(source.id().value())
        .firstName(fullName.firstName())
        .lastName(fullName.lastName())
        .middleName(fullName.middleName());
  }
}
