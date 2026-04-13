# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WildFly MicroProfile GraphQL Feature Pack — a Galleon feature pack that adds MicroProfile GraphQL support to WildFly via SmallRye GraphQL. It provides a `microprofile-graphql` Galleon layer containing the WildFly subsystem, JBoss Modules definitions, and a client subsystem.

## Build Commands

```bash
# Full build (required before running tests for the first time)
mvn -B install

# Build skipping tests
mvn -B install -DskipTests

# Run a specific integration test (requires prior full build)
mvn test -Dtest=AsyncTestCase -pl testsuite/integration

# Run tests against a different WildFly version
mvn -B test -Dversion.org.wildfly=38.0.1.Final -pl testsuite/integration,testsuite/server-tck,quickstart

# Run quickstart in dev mode
cd quickstart && mvn wildfly:provision wildfly:dev
```

After a full build, a WildFly distribution with the feature pack installed appears at `build/target/wildfly-<version>-mp-graphql-<fp-version>`.

## Module Structure

- **subsystem/** — WildFly extension implementing the `microprofile-graphql-smallrye` subsystem. Contains deployment processors that wire SmallRye GraphQL into WildFly's deployment pipeline. Package: `org.wildfly.extension.microprofile.graphql`.
- **client-subsystem/** — Separate subsystem for the SmallRye GraphQL client (Vert.x-based).
- **feature-pack/** — Galleon feature pack definition: layer spec (`feature-pack/src/main/resources/layers/standalone/microprofile-graphql/layer-spec.xml`), JBoss Module descriptors, and content packages.
- **build/** — Assembles a complete WildFly distribution with the feature pack provisioned via the Galleon Maven plugin.
- **testsuite/integration/** — Arquillian-based integration tests (async, bean validation, context propagation, federation, security, GraphQL UI, multiple deployments, etc.).
- **testsuite/server-tck/** — MicroProfile GraphQL TCK runner.
- **testsuite/client-vertx/** — Tests for the Vert.x-based GraphQL client.
- **testsuite/layer-metadata-tests/** — Validates Galleon layer metadata.
- **quickstart/** / **quickstart-client/** — Example applications.

## Architecture

The feature pack follows the standard WildFly extension pattern:
1. `MicroProfileGraphQLExtension` registers the subsystem with WildFly.
2. Deployment processors (`MicroProfileGraphQLDeploymentProcessor`, `MicroProfileGraphQLDependencyProcessor`, `GraphiQLUIDeploymentProcessor`) run during application deployment to set up SmallRye GraphQL.
3. JBoss Module descriptors under `feature-pack/src/main/resources/modules/` define module dependencies and exports for the SmallRye GraphQL runtime.
4. The `microprofile-graphql` Galleon layer declares dependencies on `cdi` and `microprofile-config`.

## Testing Patterns

- Tests use JUnit 4 + Arquillian (`@RunWith(Arquillian.class)`) with ShrinkWrap for creating test deployments.
- Tests run `@RunAsClient` — they execute outside the container and interact with the deployed application via HTTP.
- REST Assured is used to send GraphQL queries over HTTP and validate responses.
- A full `mvn -B install` is required before running individual test modules, since tests depend on the feature pack being built and provisioned.

## Code Style

- WildFly Checkstyle rules are enforced. Suppressions are in `checkstyle-suppressions.xml`.
- JBoss Logging with `@MessageLogger` / `@LogMessage` annotations (see `_private/MicroProfileGraphQLLogger.java`).

## Key Dependencies

- **SmallRye GraphQL** (`version.io.smallrye.graphql`): The MicroProfile GraphQL implementation.
- **Target WildFly** (`version.org.wildfly`): The WildFly version this feature pack is built against.
- **Vert.x** (`version.vertx`): Must match the Vert.x version bundled in WildFly.
- **Galleon** (`version.org.jboss.galleon`): Provisioning tooling.

## Limitations

- Only a single GraphQL deployment per WildFly instance is supported.
- EAR deployments are not supported.
