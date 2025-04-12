package ru.finess.finess.identity.application;

import lombok.Builder;
import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;

@Builder
public record Session(
    @NonNull UserId user, @NonNull SessionToken accessToken, SessionToken refreshToken) {}
