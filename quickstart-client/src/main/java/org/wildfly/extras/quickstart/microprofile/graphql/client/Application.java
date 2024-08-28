package org.wildfly.extras.quickstart.microprofile.graphql.client;

import jakarta.ws.rs.ApplicationPath;

import java.util.Collections;
import java.util.Set;

@ApplicationPath("/")
public class Application extends jakarta.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(FilmResource.class);
    }
}