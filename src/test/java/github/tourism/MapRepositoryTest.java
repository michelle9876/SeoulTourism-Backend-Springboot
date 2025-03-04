package github.tourism;

import github.tourism.data.entity.map.Map;
import github.tourism.data.repository.map.MapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

//@SpringBootTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class MapRepositoryTest {
    @Autowired
    private MapRepository mapRepository;

    private static final String TEST_CATEGORY = "attractions";

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 삽입
        if (mapRepository.count() == 0) {
            IntStream.rangeClosed(1, 1018).forEach(i -> {
                Map map = Map.createTestMap(TEST_CATEGORY);  // 생성자 대신 팩토리 메서드 사용
                mapRepository.save(map);
            });
        }
    }

    @Test
    void testFindByCategoryPerformance() {
        int totalRuns = 11;  // Total number of runs (10 + 1 warm-up)
        int warmUpRuns = 1;  // Number of warm-up runs to exclude from statistics

        long[] executionTimes = new long[totalRuns];

        for (int i = 0; i < totalRuns; i++) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Execute your method
            Page<Map> result = mapRepository.findByCategory(PageRequest.of(0, 10), TEST_CATEGORY);

            stopWatch.stop();
            executionTimes[i] = stopWatch.getTotalTimeMillis();

            // Log individual run time
            String runType = i < warmUpRuns ? "WARM-UP" : "TEST";
            System.out.println("Run " + (i+1) + " (" + runType + "): " + executionTimes[i] + " ms");

            // Verify correctness
            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSizeLessThanOrEqualTo(10);
            assertThat(result.getContent().get(0).getCategory()).isEqualTo(TEST_CATEGORY);

            // Optional: Add a small delay between runs
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Calculate statistics excluding warm-up runs
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        for (int i = warmUpRuns; i < totalRuns; i++) {
            totalTime += executionTimes[i];
            minTime = Math.min(minTime, executionTimes[i]);
            maxTime = Math.max(maxTime, executionTimes[i]);
        }

        int measuredRuns = totalRuns - warmUpRuns;
        double avgTime = (double) totalTime / measuredRuns;

        // Log summary statistics
        System.out.println("\n---- Performance Results ----");
        System.out.println("Measured runs: " + measuredRuns);
        System.out.println("Average execution time: " + avgTime + " ms");
        System.out.println("Minimum execution time: " + minTime + " ms");
        System.out.println("Maximum execution time: " + maxTime + " ms");
        System.out.println("Total execution time (excluding warm-up): " + totalTime + " ms");

        // Optional: Calculate standard deviation
        double variance = 0.0;
        for (int i = warmUpRuns; i < totalRuns; i++) {
            variance += Math.pow(executionTimes[i] - avgTime, 2);
        }
        double stdDev = Math.sqrt(variance / measuredRuns);
        System.out.println("Standard deviation: " + stdDev + " ms");
    }


//    @Test
//    void testLikemarkCountIncrementAndDecrement() {
//        Map map = new Map();
//        map.setCategory(TEST_CATEGORY);
//        mapRepository.save(map);
//
//        // 찜 카운트 증가 테스트
//        map.incrementLikemarkCount();
//        assertThat(map.getLikemarkCount()).isEqualTo(1);
//
//        // 찜 카운트 감소 테스트
//        map.decrementLikemarkCount();
//        assertThat(map.getLikemarkCount()).isEqualTo(0);
//    }
}
