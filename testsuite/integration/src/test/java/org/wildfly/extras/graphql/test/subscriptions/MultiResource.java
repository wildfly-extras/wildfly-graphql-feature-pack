package org.wildfly.extras.graphql.test.subscriptions;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class MultiResource {

    // TODO: if I remove this I get an error
    @Query
    public String bla() {
        return "bla";
    }

    @Subscription
    public Multi<Integer> counting() {
        return Multi.createFrom().items(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

}
