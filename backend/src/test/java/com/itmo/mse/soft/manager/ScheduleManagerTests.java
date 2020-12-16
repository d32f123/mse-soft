package com.itmo.mse.soft.manager;

import com.itmo.mse.soft.TestHelper;
import com.itmo.mse.soft.repository.ScheduleEntryRepository;
import com.itmo.mse.soft.schedule.ScheduleEntry;
import com.itmo.mse.soft.schedule.ScheduleManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ScheduleManagerTests {

    @Autowired
    ScheduleManager scheduleManager;
    @Autowired
    ScheduleEntryRepository scheduleEntryRepository;
    @Autowired
    TestHelper testHelper;

    @Test
    void contextLoads() {}

    @Test
    void shouldFindIntersection() {
        ScheduleEntry scheduleEntry = ScheduleEntry.builder()
                .timeStart(testHelper.getTimeAt(14, 20))
                .timeEnd(testHelper.getTimeAt(15, 0))
                .build();

        scheduleEntryRepository.save(scheduleEntry);

        assertThat(scheduleManager.getIntersections(
                testHelper.getTimeAt(13, 0),
                testHelper.getTimeAt(14, 40)
        )).isNotEmpty();
        assertThat(scheduleManager.getIntersections(
                testHelper.getTimeAt(14, 50),
                testHelper.getTimeAt(15, 50)
        )).isNotEmpty();
        assertThat(scheduleManager.getIntersections(
                testHelper.getTimeAt(14, 40),
                testHelper.getTimeAt(14, 50)
        )).isNotEmpty();
        assertThat(scheduleManager.getIntersections(
                testHelper.getTimeAt(12, 40),
                testHelper.getTimeAt(16, 50)
        )).isNotEmpty();
    }

}
