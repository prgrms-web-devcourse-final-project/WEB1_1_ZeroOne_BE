package com.palettee.global.cache;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.palettee.global.Const.LIKE_PREFIX;
import static com.palettee.global.Const.VIEW_PREFIX;

@Service
@NoArgsConstructor
public class MemoryCache {

    private final Map<String, Long> localCache = new ConcurrentHashMap<>();


    public void put(String key, Long value) {

        if(!localCache.containsKey(key)) {

            long adjustedValue = key.startsWith(VIEW_PREFIX) ? value : value * 5;
            localCache.put(key, adjustedValue);
        }
        else{
            // 키가 이미 존재하면 기존 값에 추가
            long adjustedValue = key.startsWith(VIEW_PREFIX) ? value : value * 5;
            localCache.put(key, localCache.get(key) + adjustedValue);  // 기존 값에 누적
        }
    }

    public void clearCache(){
        localCache.clear();
    }

    public Map<String, Long> getLocalCache() {
        return localCache;
    }

    public Map<String, Long> getViewCache(String category) {
        return localCache.entrySet().stream()
                .filter(e -> e.getKey().startsWith(VIEW_PREFIX + category))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Long> getLikeCache(String category) {
        return localCache.entrySet().stream()
                .filter(e -> e.getKey().startsWith(LIKE_PREFIX + category))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


}
