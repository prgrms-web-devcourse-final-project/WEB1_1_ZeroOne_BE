package com.palettee.global.redis.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TypeConverter {

    public static Double LocalDateTimeToDouble(LocalDateTime timeStamp) {
        long epochMilli = timeStamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        int nanos = timeStamp.getNano();
        return epochMilli + (nanos / 1000000.0);
    }

    public static String LongToString(Long id) {
        return String.valueOf(id);
    }
}
