package com.palettee.global.redis.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TypeConverter {

    public static Double LocalDateTimeToDouble(LocalDateTime timeStamp) {
        long epochMilli = timeStamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        int nanos = timeStamp.getNano();
        return epochMilli + (nanos / 1000000.0);
    }

    public static LocalDateTime StringToLocalDateTime(String timestamp) {
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));
    }

    public static String LocalDateTimeToString(LocalDateTime timestamp) {
        return String.valueOf(timestamp);
    }

    public static String LongToString(Long id) {
        return String.valueOf(id);
    }
}
