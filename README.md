# WildFly MicroProfile GraphQL Feature Pack

This repository contains a Galleon feature pack to add the MicroProfile GraphQL subsystem to WildFly.
Galleon is the tool we use internally to build the WildFly server. See the Galleon 
[documentation](https://docs.wildfly.org/galleon/) for more background about the concepts.

---------------------

## Installing the MicroProfile GraphQL Layer

To install the feature pack you need to [download Galleon](https://github.com/wildfly/galleon/releases) (use the 4.x series)
and unzip it somewhere. From now on we assume that you have added the resulting `galleon-x.y.z.Final/bin/` folder
to your path. 

The easiest way to install a WildFly Server with MicroProfile GraphQL support is 
(Download [provision.xml](provision.xml) first): 
```
galleon.sh provision /path/to/provision.xml --dir=wildfly
```                               

See [Installation guide](https://github.com/wildfly-extras/wildfly-graphql-feature-pack/wiki/Installation-guide) for more details.

**For development purposes, one way to obtain a WildFly distribution with a snapshot of this feature pack installed is to simply build the contents of this repository.
After building (`mvn clean install`), it will appear under build/target/wildfly-$VERSION. The Galleon definition of what will be included can be found in `build/pom.xml`.**

You can also build a server by using the wildfly plugin, maybe even in `dev` mode. See the Quickstart for details.

-------------

## Quickstarts
Take a look at our [Quickstart](quickstart/) for examples of how to use MicroProfile GraphQL in your applications.

-----------

## Layer in this Feature Pack 

The `microprofile-graphql` layer from this feature pack is contained in the 
[feature-pack/src/main/resources/layers/standalone](feature-pack/src/main/resources/layers/standalone)
folder.

The `microprofile-graphql` layer installs the `microprofile-graphql-smallrye` subsystem, so you can use
the MicroProfile [GraphQL](https://github.com/eclipse/microprofile-graphql) APIs 
from your application.


----
## References
* [MicroProfile GraphQL specification](https://github.com/eclipse/microprofile-graphql/releases)
* [SmallRye GraphQL Source](https://github.com/smallrye/smallrye-graphql)  
