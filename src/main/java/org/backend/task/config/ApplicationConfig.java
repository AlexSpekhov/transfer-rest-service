package org.backend.task.config;

import lombok.extern.slf4j.Slf4j;
import org.backend.task.handler.exceptionMapper.CustomExceptionMapper;
import org.backend.task.handler.ServiceHandler;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
@Slf4j
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        addRestClasses(set);
        return set;
    }


    private void addRestClasses(Set<Class<?>> resources) {
        resources.add(ServiceHandler.class);
        resources.add(CustomExceptionMapper.class);
    }
}
