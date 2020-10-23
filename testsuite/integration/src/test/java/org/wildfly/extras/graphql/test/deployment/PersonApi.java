package org.wildfly.extras.graphql.test.deployment;

import org.eclipse.microprofile.graphql.GraphQLApi;

@GraphQLApi
public interface PersonApi {

    Person get();

}
