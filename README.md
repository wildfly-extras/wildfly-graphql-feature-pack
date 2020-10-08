# WildFly MicroProfile GraphQL Feature Pack

This repository contains a Galleon feature pack to add the MicroProfile GraphQL subsystem to WildFly.
Galleon is the tool we use internally to build the WildFly server. See the Galleon 
[documentation](https://docs.wildfly.org/galleon/) for more background about the concepts.

**If you are an end-user and not a project developer (or especially interested), use the latest tag for this 
README and other information.**  

To install the feature pack you need to [download Galleon](https://github.com/wildfly/galleon/releases) (use the 4.x series)
and unzip it somewhere. The rest of this document assumes that you have added the resulting `galleon-x.y.z.Final/bin/` folder
to your path. 

The easiest way to install a WildFly Server with MicroProfile GraphQL support is 
(Download [provision.xml](provision.xml) first): 
```
galleon.sh provision /path/to/provision.xml --dir=wildfly
```
This installs a full WildFly installation and everything from this feature pack in the `wildfly/` directory.
Later we will look at what the above command means, other ways to install the server and how to tweak what is
installed. 

**For development purposes, one way to obtain a WildFly distribution with a snapshot of this feature pack installed is to simply build the contents of this repository.
After building (mvn clean install), it will appear under build/target/wildfly-$VERSION. The Galleon definition of what will be included can be found in build/pom.xml.**

-------------

## Quickstarts
Take a look at our [Quickstart](quickstart/) for examples of how to use MicroProfile GraphQL in your applications.

-----------

## Layers Introduction
To provision a server containing the MicroProfile GraphQL support, you use Galleon to first install the base 
WildFly server, and then add `layers` from this feature pack. We will see exactly how this is done later in this guide,
but it essentially allows you to create smaller servers containing exactly the parts of functionality that
you want for your application.

A layer is a unit of functionality. Each subsystem in WildFly has its own layer. For example there is 
a `cdi`  layer for the weld subsystem, a `jaxrs` layer for the jaxrs subsystem and 
an `undertow` layer for the undertow subsystem.

Layers can have dependencies between them, and when asking to install a particular layer, all the 
layer's dependencies are also installed recursively.  

There are some 'aggregate' layers, provided for convenience, such as `cloud-server` which pulls in a set of layers 
that we deem important for a server running on OpenShift. It results in a server with JAX-RS, JPA, Transactions amd 
Weld (CDI) amongst other things. 

## WildFly Layers
There is a list of all our layers defined by WildFly and WildFly Core in our 
[documentation](https://docs.wildfly.org/20/Admin_Guide.html#wildfly-galleon-layers).

However, if you want to understand better what their dependencies are, you can look at the 
layer-spec.xml for the various layers in the following locations:
* WildFly Core's [Core Feature Pack](https://github.com/wildfly/wildfly-core/tree/12.0.1.Final/core-galleon-pack/src/main/resources/layers/standalone)
* WildFly's [Servlet Feature Pack](https://github.com/wildfly/wildfly/tree/20.0.0.Final/servlet-galleon-pack/src/main/resources/layers/standalone)
* WildFly's [EE Feature Pack](https://github.com/wildfly/wildfly/tree/20.0.0.Final/ee-galleon-pack/src/main/resources/layers/standalone)
* WildFly's [Full Feature Pack](https://github.com/wildfly/wildfly/tree/20.0.0.Final/galleon-pack/src/main/resources/layers/standalone)

Note that the above links take you to the versions used for WildFly 20.0.0.Final. If you
are interested in another/newer WildFly version, adjust the tag name in the URL. 

-------------
## Layer in this Feature Pack 

The `graphql` layer from this feature pack is contained in the 
[feature-pack/src/main/resources/layers/standalone](feature-pack/src/main/resources/layers/standalone)
folder.

The `graphql` layer installs the `microprofile-graphql-smallrye` subsystem, so you can use
the MicroProfile [GraphQL](https://github.com/eclipse/microprofile-graphql) APIs 
from your application.

---------------------

## Installing the MicroProfile GraphQL Layer
Download Galleon as mentioned in the introduction. There are two main ways of provisioning a server containing the 
MicroProfile GraphQL support. The first is to provision from a file, as we saw in the introduction. The second is to 
execute all the Galleon commands individually.

In both cases we install the main WildFly server (possibly with some tweaks) and then we install the layer
from this feature pack.

Galleon can not modify the server you download from the [wildfly.org downloads page](https://wildfly.org/downloads/). 
Instead you have to install WildFly using Galleon before adding the layers from this feature pack. Both the ways shown do 
this.

### Provision from a File
The [provision.xml](provision.xml) file contains everything we need to install a server with the MicroProfile GraphQL subsystem.

It contains a reference to the WildFly feature pack. It's version is `current` which means it will download the 
latest released version (which at the time of writing is 20.0.0.Final. If you want to choose a different version, 
you can modify the file and append the version as `current:19.0.0.Final` for example. Note that WildFly 19.0.0.Final
is the first version this feature pack can be installed into, although some of the functionality requires WildFly 20.0.0.Final.

Next it contains a reference to this feature pack.

Finally it lists the layers to install:
* `cloud-profile` is from the WildFly Full Feature Pack, and is similar to `cloud-server` but smaller.
* `microprofile-graphql` installs everything from this feature pack  
  
To adjust what exactly you want to install, you can modify this file. As we saw in the introduction we can
run:
```
galleon.sh provision /path/to/provision.xml --dir=wildfly
```
This installs a full WildFly installation and everything from this feature pack in the `wildfly/` child directory
indicated by the `-dir` flag. 


### Using Galleon Commands
We need to first install our base WildFly server, and then add the layers from this feature pack. This is great
if you want to experiment with different combinations. Once you are happy you can create a provision.xml
file like we saw above.

#### Install the Base Server
First we need to install base server. To do this, we run Galleon to install the full WildFly server (the 
result will be the same as the zip you download from the [wildfly.org downloads page](https://wildfly.org/downloads/)):
```
galleon.sh install wildfly:current --dir=wildfly
```
The `wildfly:current` above tells Galleon to provision the latest version of WildFly which
at the time of writing is 20.0.0.Final. If you want to install a particular version of 
WildFly, you can append the version, e.g:

* `wildfly:current#21.0.0.Beta3-SNAPSHOT` - installs WildFly from locally build maven artifacts

Note that the minimal supported WildFly version for this feature pack is 20.0.0.Final. 

`--dir` specifies the directory to install the server into. In this case I am using 
a relative directory called `wildfly`. 

The first time you do this, it will probably take some time while it downloads everything. After the
first installation it should be a lot faster.

If you want to trim the base server that we install you can specify which layers to install by passing in 
the `--layers` option. For example to install a smaller base server, you can run the following instead:
```
galleon.sh install wildfly:current --dir=wildfly --layers=cloud-profile
```
Note that we do not install our MicroProfile GraphQL layer yet, because it is unknown in the main
WildFly feature pack. We will add it in the next step. 

If you want to keep your server as small as possible, and you miss something that is ok. You
can rerun the above command, and pass in more layers in the `--layers` option if you missed something.
   
#### Install the MicroProfile GraphQL Feature Pack
Now to install our feature pack, we can run:
```
galleon.sh install org.wildfly.extras.graphql:wildfly-microprofile-graphql-feature-pack:1.0.0.Beta2 --layers=graphql --dir=wildfly
``` 
which will install all the layers from the MicroProfile GraphQL feature pack.

----
## References
* [MicroProfile GraphQL specification](https://github.com/eclipse/microprofile-graphql/releases)
* [SmallRye GraphQL Source](https://github.com/smallrye/smallrye-graphql)  
