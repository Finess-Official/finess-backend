package ru.finess.finess.identity.application;

import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;

public record Session(@NonNull UserId user, @NonNull SessionToken accessToken) {}
