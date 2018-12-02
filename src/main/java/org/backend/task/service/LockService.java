package org.backend.task.service;


import java.util.concurrent.Callable;

public interface LockService {
    <T> T invokeConcurrently(Callable<T> task, Long... lockByIds) throws Exception;
}
