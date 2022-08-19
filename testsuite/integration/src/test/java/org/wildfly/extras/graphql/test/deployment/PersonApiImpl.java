package org.wildfly.extras.graphql.test.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
public class PersonApiImpl implements PersonApi {

    @Query
    public Person get() {
        return new Person("Dave");
    }

}
