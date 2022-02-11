package org.wildfly.extras.graphql.test.client.vertx.typesafe;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.graphql.Query;

import java.io.Closeable;

@GraphQLClientApi
public interface FunnyApi extends Closeable {

    @Query("random")
    Funny getRandomFunny();

    @Subscription("count")
    Multi<Integer> count(int fromInclusive, int toExclusive);

}
