# Zugen

[![Build Status](https://travis-ci.org/todokr/zugen.svg?branch=trunk)](https://travis-ci.org/todokr/zugen)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.todokr/sbt-zugen/badge.svg)](https://search.maven.org/artifact/io.github.todokr/sbt-zugen) 

An architecture diagram generator for Scala project.

## Documents to be generated

These following screenshots are generated in [the example Scala project](https://github.com/todokr/zugen/tree/trunk/src/sbt-test/sbt-zugen/application).

### Domain object table

A table of domain objects under the specified packages.  
This is useful as a glossary of domain terms.

- [x] List package, class/trait name, scaladoc and file path
- [x] Jump to GitHub source
- [ ] Show numbers of referrer

![Domain object table](https://user-images.githubusercontent.com/2328540/87659631-d4f4f080-c798-11ea-9ead-d8162a57aff4.png)

### Domain relation diagram

A diagram which indicates relation among domain objects.

- [x] Show class/trait name with its alias in scaladoc
- [x] Show inheritance relations
- [x] Show properties
- [x] Highlight references which is bound to outside of domain package in red
- [x] Show argument names of constructors as labels on edges
- [x] Jump to GitHub source
- [ ] Filter by package name etc.

![Domain relation diagram](https://user-images.githubusercontent.com/2328540/87659632-d4f4f080-c798-11ea-910e-40dcfac45293.png)

### Method Invocation Diagram

A diagram which shows method invocation chain.

- [x] Show project-internal method invocations
- [x] Show invocations of external libraries
- [x] Jump to GitHub source
- [ ] Highlight nodes and edges responsive to user interaction

![Metho invocation diagram](https://user-images.githubusercontent.com/2328540/87659630-d3c3c380-c798-11ea-9103-0436e92d4a40.png)

## Getting Started

### Settings
#### project/plugins.sbt

```sbt
addSbtPlugin("io.github.todokr" % "sbt-zugen" % "2020.9.1")
```

#### project/zugen.properties

```properties
domainPackages=domain                       # Package name which represent domain
domainObjectExcludePatterns=".+Repository"  # Regex patterns to exclude classes from domain relation diagram
methodInvocationRootPackage=controllers     # The root package of method invocation diagram
```

##### keys

| key                         | description                                                               | available values                                   | default           | example                                                                                      |
|-----------------------------|---------------------------------------------------------------------------|----------------------------------------------------|-------------------|----------------------------------------------------------------------------------------------|
| documentsToGenerate         | Document types to generate.<br>If empty, generates all kind of documents. | `domain-object-table`<br>`domain-relation-diagram` | *empty*           | documentsToGenerate=domain-object-table,domain-relation-diagram                              |
| domainPackages              | Package names which represent domain.                                     | *comma separated string*                           | *empty*           | domainPackages=app1.domain,app2.domain                                                       |
| domainObjectExcludePatterns | Regex patterns to exclude classes from domain relation diagram.           | *comma separated string*                           | *empty*           | domainObjectExcludePatterns=".+Repository"                                                   |
| methodInvocationRootPackage | The root package of method invocation diagram                             | *string*                                           | *empty*           | methodInvocationRootPackage=controller                                                       |
| documentPath                | Directory to output documents                                             | *string*                                           | target/zugen-docs | documentPath=docs                                                                            |
| githubBaseUrl               | The base URL of source code in GitHub repository                          | *string*                                           | *empty*           | githubBaseUrl=https://github.com/todokr/zugen/blob/trunk/src/sbt-test/sbt-zugen/application/ |

#### build.sbt

Zugen loads source code information from SemanticDB. 

```sbt
addCompilerPlugin("org.scalameta" %% "semanticdb-scalac" % "4.3.17" cross CrossVersion.full)
scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
```

### Run

```bash
$ sbt zugen
```

Then, zugen diagrams are generated under the directory specified with `documentPath` setting (default is `target/zugen-docs`).  

## Thanks
Zugen is inspired by JIG, which is a document generator for Java project and its philosophy.  

- [JIG](https://github.com/dddjava/jig)
- [コードをどまんなかに据えた設計アプローチ](https://speakerdeck.com/irof/kodowodomannakaniju-etashe-ji-apuroti)

