/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extras.quickstart.microprofile.graphql;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class AddressBookDatabase {

    // TODO REMOVE
//    @Inject
//    GraphQLProducer producer;

    private final List<Person> people = new ArrayList<>();

    @PostConstruct
    void initDatabase() {
        Person david = new Person();
        david.setName("David");
        david.setYearOfBirth(1984);
        people.add(david);

        Person peter = new Person();
        peter.setName("Peter");
        peter.setYearOfBirth(1980);
        people.add(peter);
    }

    public List<Person> getAll() {
        return Collections.unmodifiableList(people);
    }


}
