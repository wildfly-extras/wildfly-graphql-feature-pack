package org.wildfly.extras.graphql.test.client.vertx.dynamic;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@GraphQLApi
@ApplicationScoped
public class DynamicClientApi {

    @Query
    public Dummy simple() {
        Dummy ret = new Dummy();
        ret.setInteger(30);
        ret.setString("asdf");
        return ret;
    }

    @Query
    public Dummy simple2() {
        Dummy ret = new Dummy();
        ret.setInteger(31);
        ret.setString("asdfgh");
        return ret;
    }

    @Query
    public Dummy queryWithArgument(@Name(value = "number") Integer number) {
        Dummy ret = new Dummy();
        ret.setInteger(number);
        return ret;
    }

    @Subscription
    public Multi<Dummy> dummies() {
        // generate some dummies, each with the 'integer' field set to a number
        Stream<Dummy> dummies = IntStream.range(10, 20)
                .mapToObj(i -> {
                    Dummy dummy = new Dummy();
                    dummy.setInteger(i);
                    return dummy;
                });
        return Multi.createFrom().items(dummies);
    }

}
