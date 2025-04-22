package ru.finess.finess.identity.domain;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record Session(
    @NonNull UserId user, @NonNull SessionToken accessToken, SessionToken refreshToken) {}
