package org.backend.task.handler.exceptionMapper;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.time.format.DateTimeParseException;

@Provider
@Slf4j
public class CustomExceptionMapper implements ExceptionMapper<Exception> {

    public Response toResponse(Exception exception) {
        log.error(exception.getMessage(), exception);
        if (exception.getClass().equals(DateTimeParseException.class)) {
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(exception.getMessage()).type(MediaType.APPLICATION_JSON).
                    build();
        }
        if (exception.getClass().equals(RuntimeException.class)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).
                    entity(exception.getMessage()).type(MediaType.APPLICATION_JSON).
                    build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                entity(exception.getMessage()).type(MediaType.APPLICATION_JSON).
                build();
    }
}
