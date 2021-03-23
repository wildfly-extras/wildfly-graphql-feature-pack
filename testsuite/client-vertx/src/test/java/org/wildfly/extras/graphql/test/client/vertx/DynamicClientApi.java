package org.wildfly.extras.graphql.test.client.vertx;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;

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

}
