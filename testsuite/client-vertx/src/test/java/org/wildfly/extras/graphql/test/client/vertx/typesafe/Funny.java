package org.wildfly.extras.graphql.test.client.vertx.typesafe;

public class Funny {

    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Funny{" +
                "number=" + number +
                '}';
    }
}
