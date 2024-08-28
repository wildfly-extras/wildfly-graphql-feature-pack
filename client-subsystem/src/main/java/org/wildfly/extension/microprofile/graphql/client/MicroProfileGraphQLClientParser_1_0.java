package org.wildfly.extension.microprofile.graphql.client;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

public class MicroProfileGraphQLClientParser_1_0 extends PersistentResourceXMLParser {

    public static final String NAMESPACE = "urn:wildfly:microprofile-graphql-client-smallrye:1.0";

    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription =  org.jboss.as.controller.PersistentResourceXMLDescription.builder(MicroProfileGraphQLClientExtension.SUBSYSTEM_PATH, NAMESPACE)
                .build();
    }

    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return xmlDescription;
    }

}
