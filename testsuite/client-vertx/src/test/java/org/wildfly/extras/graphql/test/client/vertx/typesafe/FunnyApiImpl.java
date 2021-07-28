package org.wildfly.extras.graphql.test.client.vertx.typesafe;

import java.util.concurrent.ThreadLocalRandom;

public class FunnyApiImpl implements FunnyApi {

    @Override
    public Funny getRandomFunny() {
        Funny funny = new Funny();
        funny.setNumber(ThreadLocalRandom.current().nextInt(1, 1000));
        return funny;
    }

}
