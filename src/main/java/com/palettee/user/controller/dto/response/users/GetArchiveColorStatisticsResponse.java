package com.palettee.user.controller.dto.response.users;

import com.palettee.archive.domain.*;
import java.util.*;
import java.util.stream.*;

public record GetArchiveColorStatisticsResponse(
        long RED,
        long YELLOW,
        long GREEN,
        long BLUE,
        long PURPLE
) {

    public static GetArchiveColorStatisticsResponse of(List<Archive> archives) {

        Map<ArchiveType, Long> colorMap = archives.stream()
                .filter(archive -> !archive.getType().equals(ArchiveType.NO_COLOR))
                .collect(Collectors.groupingBy(Archive::getType, Collectors.counting()));

        return new GetArchiveColorStatisticsResponse(
                getColor(colorMap, ArchiveType.RED),
                getColor(colorMap, ArchiveType.YELLOW),
                getColor(colorMap, ArchiveType.GREEN),
                getColor(colorMap, ArchiveType.BLUE),
                getColor(colorMap, ArchiveType.PURPLE)
        );
    }

    private static long getColor(Map<ArchiveType, Long> colorMap, ArchiveType type) {
        return colorMap.getOrDefault(type, 0L);
    }
}
