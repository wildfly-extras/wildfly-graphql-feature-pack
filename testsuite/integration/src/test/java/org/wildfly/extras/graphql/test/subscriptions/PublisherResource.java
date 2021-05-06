package org.wildfly.extras.graphql.test.subscriptions;

import io.smallrye.graphql.api.Subscription;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

@GraphQLApi
public class PublisherResource {

    // TODO: if I remove this I get an error
    @Query
    public String bla() {
        return "bla";
    }

    @Subscription
    public Publisher<Integer> counting() {
        return subscriber -> {
            for (int i = 0; i < 10; i++) {
                subscriber.onNext(i);
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            subscriber.onComplete();
        };
    }

}
