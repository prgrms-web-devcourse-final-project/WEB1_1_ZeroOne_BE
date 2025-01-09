package com.palettee.global.redis.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TypeConverter {

    public static Double LocalDateTimeToDouble(LocalDateTime timeStamp) {
        return ((Long) timeStamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).doubleValue();
    }

    public static String LongToString(Long id) {
        return String.valueOf(id);
    }
}
