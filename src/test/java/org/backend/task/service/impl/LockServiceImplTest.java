package org.backend.task.service.impl;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class LockServiceImplTest extends BaseTest {


    private static Map<Long, Integer> runningMap = new HashMap<>();

    @Test
    public void successTest() throws Exception {

        Integer numIterations = 10000;

        Long firstId = 1L;
        Long secondId = 2L;

        runningMap.put(firstId, 0);
        runningMap.put(secondId, 0);

        Callable<Void> call1 = createCallable(numIterations / 2, firstId);
        Callable<Void> call2 = createCallable(numIterations / 2, secondId);
        Callable<Void> call3 = createCallable(numIterations / 2, firstId, secondId);

        ExecutorService executorService = Executors.newFixedThreadPool(3);

//        executorService.submit(call1);
//        executorService.submit(call2);
//        executorService.submit(call3);

        executorService.submit(() -> lockService.invokeConcurrently(call1, 1L));
        executorService.submit(() -> lockService.invokeConcurrently(call2, 2L));
        executorService.submit(() -> lockService.invokeConcurrently(call3, 1L, 2L));

        TimeUnit.SECONDS.sleep(3);

        assertEquals(runningMap.get(firstId).intValue(), numIterations.intValue());
        assertEquals(runningMap.get(secondId).intValue(), numIterations.intValue());

    }

    @Test
    public void failTest() throws Exception {
        try {
            lockService.invokeConcurrently(() -> {
                throw new Exception();
            });
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
            assertEquals(ex.getMessage(), "Invalid ids");
        }
    }

    private Callable<Void> createCallable(int numIterations, Long... ids) {
        return () -> {
            for (Long id : ids) {
                runningMap.putIfAbsent(id, 0);
            }

            for (int i = 0; i < numIterations; i++) {
                for (Long id : ids) {
                    Integer value = runningMap.get(id);
                    Integer newValue = value + 1;
                    runningMap.put(id, newValue);
                }
            }

            return null;
        };
    }
}
