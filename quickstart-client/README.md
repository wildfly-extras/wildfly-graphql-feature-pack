# GraphQL Client quickstart 

## Prerequisites
- JDK 11+
- Maven

## Quickstart's compatibility with server-side quickstart
This quickstart requires the server-side quickstart implementation to function properly.  Instructions for deploying the feature pack on the server-side can be found in the server-side quickstart's [README](../quickstart/README.md).


## Building and deployment

The [main README](../README.md) contains information about the layers in this feature pack. You can use the `wildfly-maven-plugin` to build and run the server with the feature pack, and deploy the quickstart war.

```
mvn wildfly:provision wildfly:dev
```


## Functionality
The quickstart-client.war application exposes various REST endpoints that demonstrate interacting with the server-side GraphQL API through the SmallRye GraphQL Client.
The endpoints utilize both typesafe and dynamic client approaches.
### Typesafe API

The typesafe client functions like a MicroProfile REST Client but is tailored for GraphQL endpoints. To use it, you will need domain model classes that match the GraphQL schema.

Think of a client instance as a proxy. You interact with it as you would a regular Java object, and it translates your calls into GraphQL operations.

It directly handles domain classes, translating input and output data between Java objects and their representations in the GraphQL query language.
#### Example

Let's start by defining a domain class `Film` to represent the data we will be working with.
```java
package org.wildfly.extras.quickstart.microprofile.graphql.client;

import java.time.LocalDate;

public class Film {
    String title;
    Integer episodeID;
    String director;
    LocalDate releaseDate;
    String desc;
}
```
Next, create a GraphQL client API interface `FilmClientApi` to define the GraphQL operations:
```java
@GraphQLClientApi
interface FilmClientApi {
    @Query
    List<Film> getAllFilms();
}
```

Finally, implement a REST endpoint to expose the GraphQL client's functionality:
```java
@GET
@Path("/typesafe/films")
@Produces(MediaType.APPLICATION_JSON)
public List<Film> getAllFilms() {
    FilmClientApi client = TypesafeGraphQLClientBuilder.newBuilder()
            .endpoint(URL) // http://localhost:8080/quickstart/graphql
            .build(FilmClientApi.class);
    return client.getAllFilms();
}
```
You can test the endpoint using a tool like `curl`:
```
curl localhost:8080/quickstart-client/typesafe/films
```
The typesafe client will automatically generate the corresponding GraphQL query based on the operation's return type and domain classes:
```
query allFilms {
    allFilms {
        title
        pisodeID
        director
        releaseDate
        desc
    } 
}
```
The proxy will then send this query as part of the GraphQL request to the server. The server will process the request and return a GraphQL response.
The response will be deserialized into Java objects.

Other endpoints:
- **`localhost:8080/typesafe/films/{id}`:** Retrieves a specific film by its index.
- **`localhost:8080/typesafe/delete/hero/{id}`:** Deletes a hero by its index.
- **`localhost:8080/typesafe/heroes/{surname}`:** Retrieves a list of heroes with a specific surname.

> [NOTE]
> The `VertxTypesafeGraphQLClientBuilder` (*Vert.x*'s typesafe implementation) allows to inject a pre-build `ClientModels` bean instance–which has been generated during deployment process via Jandex API.
> This means you can provide the pre-generated GraphQL queries directly to the typesafe client builder, bypassing the default generation process of using Java Reflection during runtime.
> ```java
> @Inject
> ClientModels clientmodels;
> // ...
> FilmClientApi client = new VertxTypesafeGraphQLClientBuilder()
>                        .clientModels(clientModels)
>                        .endpoint(URL)
>                        .build(FilmClientApi.class);
> // ...
>```

### Dynamic API
Unlike the typesafe API, the dynamic client does not require a client API interface or domain classes. It operates directly with abstract representations of GraphQL documents, constructed using a domain-specific language (DSL).
Exchanged objects are treated as abstract `JsonObject`, but can be converted to concrete model objects if necessary.
#### Example
```java
@GET
@Path("/dynamic/films/{id}")
@Produces(MediaType.APPLICATION_JSON)
public Film getFilmDynamic(@PathParam("id") int id) throws ExecutionException, InterruptedException {
    try (VertxDynamicGraphQLClient dynamicGraphQLClient =
                 (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                         .url(URL)
                         .build()
    ) {

        Variable filmId = var("filmId", nonNull(ScalarType.GQL_INT));

        Document query = document(
                operation("film", vars(filmId),
                        field("film", args(arg("filmId", filmId)),
                                field("title"),
                                field("episodeID"),
                                field("director"),
                                field("releaseDate"),
                                field("desc")
                        )
                )
        );
        Response response = dynamicGraphQLClient.executeSync(query, Collections.singletonMap("filmId", id));
        return response.getObject(Film.class, "film");
    }
}
```
You can test the endpoint using a tool like `curl`:
```
curl localhost:8080/quickstart-client/dynamic/films/{id}
```




