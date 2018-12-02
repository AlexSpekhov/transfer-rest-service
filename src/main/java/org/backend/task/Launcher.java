package org.backend.task;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import lombok.extern.slf4j.Slf4j;
import org.backend.task.config.ApplicationConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

@Slf4j
public class Launcher {

    public static void main(String[] args) {
        int port = 8082;
        Server server = startUp(port);
        try {
            server.join();
        } catch (InterruptedException e) {
            server.destroy();
        }
    }

    public static Server startUp(int port) {
        Server jettyServer = new Server(port);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("/");

        ServletHolder servletHolder = webAppContext.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter("javax.ws.rs.Application", ApplicationConfig.class.getName());
        servletHolder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        servletHolder.setInitOrder(0);

        jettyServer.setHandler(webAppContext);

        try {
            log.info("starting jetty on port {}", port);
            jettyServer.start();
            servletHolder.start();
            servletHolder.doStart();
        } catch (Exception e) {
            jettyServer.destroy();
        }
        return jettyServer;
    }
}
