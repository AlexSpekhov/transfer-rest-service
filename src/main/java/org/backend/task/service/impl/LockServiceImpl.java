package org.backend.task.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.backend.task.service.LockService;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LockServiceImpl implements LockService {
    private final Map<Long, Lock> locksMap = new ConcurrentHashMap<>();

    @Override
    public <T> T invokeConcurrently(Callable<T> task, Long... lockByIds) throws Exception {
        if (lockByIds == null || lockByIds.length == 0) {
            throw new IllegalArgumentException("Invalid ids");
        }
        Long[] ids = lockByIds.clone();

        try {
            Arrays.stream(ids)
                    .map(id -> locksMap.computeIfAbsent(id, element -> new ReentrantLock(true)))
                    .forEach(Lock::lock);
            return task.call();
        } finally {
            Arrays.stream(ids)
                    .map(id -> locksMap.computeIfAbsent(id, element -> new ReentrantLock(true)))
                    .forEach(Lock::unlock);
        }
    }
}
