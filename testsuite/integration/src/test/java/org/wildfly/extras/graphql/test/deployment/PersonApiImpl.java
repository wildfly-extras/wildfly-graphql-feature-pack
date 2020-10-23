package org.wildfly.extras.graphql.test.deployment;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class PersonApiImpl implements PersonApi {

    @Query
    public Person get() {
        return new Person("Dave");
    }

}
