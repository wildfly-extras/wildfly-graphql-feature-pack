package org.wildfly.extras.graphql.test.client.jaxrs;

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
