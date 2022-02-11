package org.wildfly.extras.graphql.test.client.vertx.typesafe;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.concurrent.ThreadLocalRandom;

@GraphQLApi
public class FunnyApiImpl {

    @Query("random")
    public Funny getRandomFunny() {
        Funny funny = new Funny();
        funny.setNumber(ThreadLocalRandom.current().nextInt(1, 1000));
        return funny;
    }

    @Subscription("count")
    public Multi<Integer> count(int fromInclusive, int toExclusive) {
        return Multi.createFrom().range(fromInclusive, toExclusive);
    }

}
