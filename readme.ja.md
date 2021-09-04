# Zugen

[![Build Status](https://travis-ci.org/todokr/zugen.svg?branch=trunk)](https://travis-ci.org/todokr/zugen)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.todokr/sbt-zugen/badge.svg)](https://search.maven.org/artifact/io.github.todokr/sbt-zugen) 

[English ver.](./readme.md)

Scalaプロジェクト向けの、アーキテクチャ図やドキュメントを生成するsbtプラグインです。

## 生成されるアーキテクチャ図やドキュメントの種類

以下のスクリーンショットは [サンプルプロジェクト](https://github.com/todokr/zugen/tree/trunk/src/sbt-test/sbt-zugen/application)に対してZugenを実行した結果です。

### Domain object table

ドメインオブジェクトが含まれるとして指定したパッケージ以下の、オブジェクトたちの一覧表です。
ドメインオブジェクトがドメインの用語と対応関係にあるか、などを確認する際に便利です。

- [x] パッケージやclass/trait名、scaladoc、ファイルパスの一覧
- [x] GitHubのソースへのジャンプ

![Domain object table](https://user-images.githubusercontent.com/2328540/87659631-d4f4f080-c798-11ea-9ead-d8162a57aff4.png)

### Domain relation diagram

ドメインオブジェクト同士の関連を示す図です。

- [x] class/trait名とscaladocの表示
- [x] 継承関係の図示
- [x] classやtraitのプロパティの表示
- [x] ドメイン以外のパッケージに依存するドメインオブジェクトの参照を赤くハイライト
- [x] GitHubのソースへのジャンプ

![Domain relation diagram](https://user-images.githubusercontent.com/2328540/87659632-d4f4f080-c798-11ea-910e-40dcfac45293.png)

### Method Invocation Diagram

特定のパッケージ（controller用パッケージなど）を起点に、メソッドの呼び出し関係を示す図です。

- [x] プロジェクト内部のクラス同士の呼び出しを可視化
- [x] 外部ライブラリの呼び出しを（薄いグレーで）可視化
- [x] GitHubのソースへのジャンプ

![Method invocation diagram](https://user-images.githubusercontent.com/2328540/87659630-d3c3c380-c798-11ea-9103-0436e92d4a40.png)

## Getting Started

### Settings
#### project/plugins.sbt

```sbt
addSbtPlugin("io.github.todokr" % "sbt-zugen" % "2020.11.1")
```

#### project/zugen.properties

```properties
domainPackages=example.domain                   # Package name which represent domain
domainObjectExcludePatterns=".+Repository"      # Regex patterns to exclude classes from domain relation diagram
methodInvocationRootPackage=example.controllers # The root package of method invocation diagram
```

##### keys

| key                         | description                                                               | available values                                                                  | default           | example                                                                                      |
|-----------------------------|---------------------------------------------------------------------------|-----------------------------------------------------------------------------------|-------------------|----------------------------------------------------------------------------------------------|
| documentsToGenerate         | 出力するドキュメントタイプです。<br>空の場合、全種類のドキュメントが生成されます。         | `domain-object-table`<br>`domain-relation-diagram`<br>`method-invocation-diagram` | *empty*           | documentsToGenerate=domain-object-table,domain-relation-diagram,method-invocation-diagram    |
| domainPackages              | ドメインオブジェクトが含まれるパッケージを指定します。                                | *comma separated string*                                                          | *empty*           | domainPackages=app1.domain,app2.domain                                                       |
| domainObjectExcludePatterns | Domain relation diagram に表示させないクラスを、正規表現で指定できます。              | *comma separated string*                                                          | *empty*           | domainObjectExcludePatterns=".+Repository"                                                   |
| methodInvocationRootPackage | Method invocation diagram における呼び出しの起点となるパッケージです。               | *string*                                                                          | *empty*           | methodInvocationRootPackage=controller                                                       |
| documentPath                | Zugenのドキュメントを出力するディレクトリです。                                    | *string*                                                                          | target/zugen-docs | documentPath=docs                                                                            |
| githubBaseUrl               | GitHubのソースへのジャンプを行うためのベースとなる、リポジトリURLです                   | *string*                                                                          | *empty*           | githubBaseUrl=https://github.com/todokr/zugen/blob/trunk/src/sbt-test/sbt-zugen/application/ |

#### build.sbt

Zugen はソースコードの情報を [SemanticDB](https://scalameta.org/docs/semanticdb/guide.html) から収集しています。
SemanticDB のファイルを生成する手段は2つあります。

##### sbtオプション
1.13より、sbt は SemanticDB の生成をサポートしています。

```sbt
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.4.27"
scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
```

##### compiler plugin

```sbt
addCompilerPlugin("org.scalameta" %% "semanticdb-scalac" % "4.4.27" cross CrossVersion.full)
scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
```

### Run

```bash
$ sbt zugen
```

実行すると、`documentPath` で設定したディレクトリ （デフォルトは `target/zugen-docs`) に、Zugenのドキュメントが生成されます。

質問などは気軽に [Twitter](https://twitter.com/todokr)でご連絡ください。

## Thanks
Zugen Javaプロジェクトのドキュメント生成ツールである JIG の哲学にインスパイアされています。

- [JIG](https://github.com/dddjava/jig)
- [コードをどまんなかに据えた設計アプローチ](https://speakerdeck.com/irof/kodowodomannakaniju-etashe-ji-apuroti)

