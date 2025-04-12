package ru.finess.finess.common.date;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

  private static final ZoneOffset CURRENT_ZONE_OFFSET = OffsetDateTime.now().getOffset();

  public static OffsetDateTime toOffsetDateTime(Date date) {
    return date.toInstant().atOffset(CURRENT_ZONE_OFFSET);
  }
}
