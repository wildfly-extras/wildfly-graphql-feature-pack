package org.wildfly.extras.quickstart.microprofile.graphql.client;

import java.util.List;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.wildfly.extras.quickstart.microprofile.graphql.client.model.Film;
import org.wildfly.extras.quickstart.microprofile.graphql.client.model.Hero;

@GraphQLClientApi
interface FilmClientApi {
    @Query
    List<Film> getAllFilms();

    @Query
    Film getFilm(@Name("filmId") int id);

    @Mutation
    Hero deleteHero(int id);

    @Query
    List<Hero> getHeroesWithSurname(String surname);
}
