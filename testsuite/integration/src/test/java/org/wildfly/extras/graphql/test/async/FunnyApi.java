package org.wildfly.extras.graphql.test.async;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.concurrent.CompletionStage;

@GraphQLApi
public interface FunnyApi {

    @Query("randomAsync")
    CompletionStage<Funny> getRandomFunnyAsync();

    @Query("scalarAsync")
    CompletionStage<String> getStringAsync();

    @Query("errorAsync")
    CompletionStage<String> getErrorAsync();

    @Query("random")
    Funny getRandomFunny();

}
