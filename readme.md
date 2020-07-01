# Zugen

[![Build Status](https://travis-ci.org/todokr/zugen.svg?branch=master)](https://travis-ci.org/todokr/zugen)

A tool for generating Zu(図) of Scala project.

## Documents to be generated

These screenshots are generated in [the example Scala project](https://github.com/todokr/zugen/blob/master/src/sbt-test/sbt-zugen/simple/build.sbt).

### Domain object table

A table of domain objects under the specified packages.

- [x] List package, object/trait/class name, scaladoc and file path
- [ ] Show referrer count

![Domain object table](https://raw.githubusercontent.com/todokr/zugen/master/docs/domain-object-table.png)

### Domain relation diagram

A diagram which indicates relation among domain objects.


- [x] Show inheritance relations
- [x] Show properties
- [x] Highlight references which is bound to outside of domain package in red
- [ ] Handle container class (Seq[T], etc.)

![Domain relation diagram](https://raw.githubusercontent.com/todokr/zugen/master/docs/domain-relation-diagram.png)

## Setup

### `project/plugins.sbt`

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.todokr/sbt-zugen/badge.svg)](https://search.maven.org/artifact/io.github.todokr/sbt-zugen)

```sbt
addSbtPlugin("io.github.todokr" % "sbt-zugen" % "(version)")
```

### `build.sbt`
```sbt
// zugen loads source code infomation from SemainticDB
addCompilerPlugin("org.scalameta" %% "semanticdb-scalac" % "4.3.17" cross CrossVersion.full)
scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")

enablePlugins(ZugenPlugin)
zugenDomainPackages := Seq("your.projects.domain", "your.projects.other.domain")
```

## Run

```bash
$ sbt zugen
```

Setting keys are defined in [here](https://github.com/todokr/zugen/blob/master/src/main/scala/zugen/sbt/ZugenPlugin.scala#L16-L19).

## Thanks
Zugen is inspired by JIG, which is a document generator for Java project and its philosophy.  

- [JIG](https://github.com/dddjava/jig)
- [コードをどまんなかに据えた設計アプローチ](https://speakerdeck.com/irof/kodowodomannakaniju-etashe-ji-apuroti)

