The `build` module is used as an easy way to obtain a WildFly distribution containing the feature pack.
Resulting files from this module:

- `wildfly-{WILDFLY_VERSION}-mp-graphql-{FEATUREPACK_VERSION}`
  - Thin (maven-based) distribution of WildFly with the feature pack installed
- `wildfly-{WILDFLY_VERSION}-mp-graphql-{FEATUREPACK_VERSION}-full`
  - Full (including all JARs) distribution of WildFly with the feature pack installed
- `wildfly-{WILDFLY_VERSION}-mp-graphql-{FEATUREPACK_VERSION}.zip`
  - Zip with the thin (maven-based) distribution of WildFly with the feature pack installed 
- `wildfly-{WILDFLY_VERSION}-mp-graphql-{FEATUREPACK_VERSION}-full.zip`
  - Zip with the full distribution of WildFly with the feature pack installed
- `wildfly-{WILDFLY_VERSION}-mp-graphql-{FEATUREPACK_VERSION}-overlay.zip`
  - A simple overlay zip that can be unzipped over any WildFly distribution that provides the necessary files for the feature pack.
  Note that this is not a full-fledged way to install the feature pack, because you will still need to update your WildFly configuration
  in order to activate GraphQL capabilities!