package org.wildfly.extras.graphql.test.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadLocalRandom;

public class FunnyApiImpl implements FunnyApi {

    @Override
    public CompletionStage<Funny> getRandomFunnyAsync() {
        return CompletableFuture.supplyAsync(() -> {
            Funny funny = new Funny();
            funny.setNumber(ThreadLocalRandom.current().nextInt(0, 1000) + 1000);
            return funny;
        });
    }

    @Override
    public CompletionStage<String> getStringAsync() {
        return CompletableFuture.completedFuture("Hello");
    }

    @Override
    public CompletionStage<String> getErrorAsync() {
        throw new IllegalStateException();
    }

    @Override
    public Funny getRandomFunny() {
        Funny funny = new Funny();
        funny.setNumber(ThreadLocalRandom.current().nextInt(0, 1000));
        return funny;
    }

}
