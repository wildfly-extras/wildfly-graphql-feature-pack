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
            subscriber.onSubscribe(new org.reactivestreams.Subscription() {

                int i = -1;

                @Override
                public void request(long l) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(i > 9) {
                        subscriber.onComplete();
                    } else {
                        i++;
                        subscriber.onNext(i);
                    }
                }

                @Override
                public void cancel() {
                }
            });
        };
    }

}
