package github.tourism.service.redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLikeService {
    private final RedisTemplate<String, Integer> redisTemplate;

    public RedisLikeService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Redis에 찜 카운트 저장
    public void saveLikeCount(String placeName, Integer likeCount) {
        ValueOperations<String, Integer> ops = redisTemplate.opsForValue();
        ops.set(getRedisKey(placeName), likeCount, 1, TimeUnit.HOURS); // 캐시 만료 시간 1시간
    }

    // Redis에서 찜 카운트 가져오기
    public Integer getLikeCount(String placeName) {
        ValueOperations<String, Integer> ops = redisTemplate.opsForValue();
        return ops.get(getRedisKey(placeName));
    }

    // 찜 카운트 증가
    public Integer incrementLikeCount(String placeName) {
        return Math.toIntExact(redisTemplate.opsForValue().increment(getRedisKey(placeName)));
    }

    // 찜 카운트 감소
    public Integer decrementLikeCount(String placeName) {
        return Math.toIntExact(redisTemplate.opsForValue().decrement(getRedisKey(placeName)));
    }

    // Redis 키 생성 (일관성을 위해)
    private String getRedisKey(String placeName) {
        return "likemark:" + placeName;
    }

    // Redis에서 키 삭제
    public void deleteLikeCount(String placeName) {
        redisTemplate.delete(getRedisKey(placeName));
    }
}
