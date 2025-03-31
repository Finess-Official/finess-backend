package ru.finess.finess.common.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;

public interface UseCase<R, E, P> {

  Result<R, E> execute(@NonNull P parameters);
}
