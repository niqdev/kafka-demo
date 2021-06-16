lazy val V = new {
  val scala = "2.13.3"

  val logback      = "1.2.3"
  val scalaLogging = "3.9.2"
  val logEffect    = "0.13.2"

  val avro4s    = "4.0.0"
  val kafka     = "2.5.1"
  val confluent = "5.5.1"

  val circe      = "0.13.0"
  val ciris      = "1.2.1"
  val refined    = "0.9.17"
  val newtype    = "0.4.4"
  val cats       = "2.2.0"
  val catsEffect = "2.2.0"
  val catsRetry  = "1.1.1"
  val zio        = "1.0.1"
  val zioLogging = "0.5.11"
  val zioConfig  = "1.0.0-RC27"

  // compatibility issues
  val cakeSolutions = new {
    val version   = "2.0.0"
    val kafka     = "2.0.0"
    val confluent = "5.0.0"
  }

  val scalaTest     = "3.1.0"
  val embeddedKafka = "5.5.1"
}

lazy val common = project
  .in(file("common"))
  .settings(
    javacOptions ++= Seq("-source", "11"),
    libraryDependencies ++= Seq(
      "ch.qos.logback"              % "logback-classic" % V.logback,
      "com.typesafe.scala-logging" %% "scala-logging"   % V.scalaLogging,
      "org.scalatest"              %% "scalatest"       % V.scalaTest % Test
    )
  )

lazy val avro = project
  .in(file("avro"))
  // test dependencies are excluded by default otherwise
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(SbtAvro)
  .disablePlugins(SbtAvrohugger)
  .settings(
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core" % V.avro4s
    )
  )

lazy val kafka = project
  .in(file("kafka"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    resolvers ++= Seq(
      Resolver.bintrayRepo("cakesolutions", "maven")
    ),
    libraryDependencies ++= Seq(
      "net.cakesolutions" %% "scala-kafka-client"         % V.cakeSolutions.version,
      "net.cakesolutions" %% "scala-kafka-client-testkit" % V.cakeSolutions.version % Test
    )
  )

lazy val `schema-registry` = project
  .in(file("schema-registry"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(SbtAvrohugger)
  // avoid issue XXX is already defined as class XXX
  .disablePlugins(SbtAvro)
  .settings(
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven/",
      Resolver.bintrayRepo("cakesolutions", "maven")
    ),
    libraryDependencies ++= Seq(
      "org.apache.kafka"   % "kafka-clients"                % V.cakeSolutions.kafka,
      "io.confluent"       % "kafka-avro-serializer"        % V.cakeSolutions.confluent,
      "io.confluent"       % "kafka-schema-registry-client" % V.cakeSolutions.confluent,
      "net.cakesolutions" %% "scala-kafka-client"           % V.cakeSolutions.version,
      "net.cakesolutions" %% "scala-kafka-client-testkit"   % V.cakeSolutions.version % Test
    ),
    // sbt-avrohugger: SpecificRecord
    sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue
  )

lazy val streams = project
  .in(file("streams"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.kafka"  % "kafka-streams"            % V.kafka,
      "org.apache.kafka" %% "kafka-streams-scala"      % V.kafka,
      "org.apache.kafka"  % "kafka-streams-test-utils" % V.kafka % Test
    )
  )

lazy val `streams-json-avro` = project
  .in(file("streams-json-avro"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven/",
      "jitpack.io" at "https://jitpack.io"
    ),
    libraryDependencies ++= Seq(
      "org.apache.kafka"        %% "kafka-streams-scala"                    % V.kafka,
      "io.confluent"             % "kafka-streams-avro-serde"               % V.confluent,
      "io.circe"                %% "circe-core"                             % V.circe,
      "io.circe"                %% "circe-generic"                          % V.circe,
      "io.circe"                %% "circe-parser"                           % V.circe,
      "com.sksamuel.avro4s"     %% "avro4s-core"                            % V.avro4s,
      "io.github.embeddedkafka" %% "embedded-kafka-schema-registry-streams" % V.embeddedKafka % Test
    )
  )

lazy val `cats-kafka-streams` = project
  .in(file("cats-kafka-streams"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation",
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Xlint",
      "-Xfatal-warnings",
      "-Ypartial-unification",
      "-language:postfixOps"
    ),
    // required by newtype: on 2.13.x use "-Ymacro-annotations"
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven/"
    ),
    libraryDependencies ++= Seq(
      "io.laserdisc"        %% "log-effect-fs2"           % V.logEffect,
      "org.typelevel"       %% "cats-core"                % V.cats,
      "org.typelevel"       %% "cats-effect"              % V.catsEffect,
      "com.github.cb372"    %% "cats-retry"               % V.catsRetry,
      "is.cir"              %% "ciris"                    % V.ciris,
      "is.cir"              %% "ciris-refined"            % V.ciris,
      "eu.timepit"          %% "refined"                  % V.refined,
      "io.estatico"         %% "newtype"                  % V.newtype,
      "org.apache.kafka"    %% "kafka-streams-scala"      % V.kafka,
      "io.confluent"         % "kafka-streams-avro-serde" % V.confluent,
      "com.sksamuel.avro4s" %% "avro4s-core"              % V.avro4s
    )
  )

lazy val `zio-kafka-streams` = project
  .in(file("zio-kafka-streams"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven/"
    ),
    libraryDependencies ++= Seq(
      "dev.zio"             %% "zio"                      % V.zio,
      "dev.zio"             %% "zio-logging"              % V.zioLogging,
      "dev.zio"             %% "zio-config"               % V.zioConfig,
      "dev.zio"             %% "zio-config-refined"       % V.zioConfig,
      "org.apache.kafka"    %% "kafka-streams-scala"      % V.kafka,
      "io.confluent"         % "kafka-streams-avro-serde" % V.confluent,
      "com.sksamuel.avro4s" %% "avro4s-core"              % V.avro4s
    )
  )

lazy val `interactive-queries` = project
  .in(file("interactive-queries"))
  .dependsOn(common % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.kafka"  % "kafka-streams"            % V.kafka,
      "org.apache.kafka" %% "kafka-streams-scala"      % V.kafka,
      "org.apache.kafka"  % "kafka-streams-test-utils" % V.kafka % Test
    )
  )

lazy val root = project
  .in(file("."))
  .aggregate(
    avro,
    kafka,
    `schema-registry`,
    streams,
    `streams-json-avro`,
    `cats-kafka-streams`,
    `zio-kafka-streams`
  )
  .settings(
    organization := "com.kafka.demo",
    name := "kafka-scala-examples",
    scalaVersion := V.scala,
    version := "0.1",
    parallelExecution := false,
    addCommandAlias("checkFormat", ";scalafmtCheckAll;scalafmtSbtCheck"),
    addCommandAlias("format", ";scalafmtAll;scalafmtSbt"),
    addCommandAlias("build", ";checkFormat;clean;test")
  )
