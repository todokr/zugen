# Zugen

[![Build Status](https://travis-ci.org/todokr/zugen.svg?branch=master)](https://travis-ci.org/todokr/zugen)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.todokr/sbt-zugen/badge.svg)](https://search.maven.org/artifact/io.github.todokr/sbt-zugen)

An architecture diagram generator for Scala project.

## Documents to be generated

These screenshots are generated in [the example Scala project](https://github.com/todokr/zugen/tree/trunk/src/sbt-test/sbt-zugen/application).

### Domain object table

A table of domain objects under the specified packages.  
This is useful as a glossary of domain terms.

- [x] List package, class/trait name, scaladoc and file path
- [ ] Sort and filter by package name etc.
- [ ] Show numbers of referrer

![Domain object table](https://user-images.githubusercontent.com/2328540/87234372-24d75e80-c40b-11ea-969a-5768b5ff6cac.png)

### Domain relation diagram

A diagram which indicates relation among domain objects.

- [x] Show class/trait name with its alias in scaladoc
- [x] Show inheritance relations
- [x] Show properties
- [x] Highlight references which is bound to outside of domain package in red
- [ ] Show argument names of constructors as labels on edges
- [ ] Filter by package name etc.

![Domain relation diagram](https://user-images.githubusercontent.com/2328540/87234357-f0fc3900-c40a-11ea-8100-ba161712c28e.png)

## Getting Started

### Settings
#### project/plugins.sbt

```sbt
addSbtPlugin("io.github.todokr" % "sbt-zugen" % "0.1")
```

#### project/zugen.properties

```properties
domainPackages=domain
domainObjectExcludePatterns=".+Repository"
```

##### keys

| key                         | description                                                               | available values                                   | default           | example                                                         |
|-----------------------------|---------------------------------------------------------------------------|----------------------------------------------------|-------------------|-----------------------------------------------------------------|
| documentsToGenerate         | Document types to generate.<br>If empty, generates all kind of documents. | `domain-object-table`<br>`domain-relation-diagram` | *empty*           | documentsToGenerate=domain-object-table,domain-relation-diagram |
| domainPackages              | Package names which represent domain.                                     | *comma separated string*                           | *empty*           | domainPackages=app1.domain,app2.domain                          |
| domainObjectExcludePatterns | Regex patterns to exclude classes from domain relation diagram.           | *comma separated string*                           | *empty*           | domainObjectExcludePatterns=".+Repository"                      |
| documentPath                | Directory to output documents                                             | *string*                                           | target/zugen-docs | documentPath=docs                                               |

#### build.sbt

Zugen loads source code infomation from SemainticDB. 

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

