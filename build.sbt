val scala3Version = "3.2.1"

val circeVersion = "0.14.1"
val emilVersion = "0.10.0-M3"
val http4sVersion = "0.23.0"
val mongo4catsVersion = "0.4.2"
val sttpVersion = "3.8.5"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Beat the Queue",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      // java dependencies
      "ch.qos.logback" % "logback-classic" % "1.2.6",
      "nl.martijndwars" % "web-push" % "5.1.1"
    ) ++ Seq(
      // scala 3 dependencies
      "com.github.eikek" %% "emil-common" % emilVersion,
      "com.github.eikek" %% "emil-javamail" % emilVersion,
      "com.softwaremill.quicklens" %% "quicklens" % "1.8.2",
      "com.softwaremill.sttp.client3" %% "cats" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.github.jmcardon" %% "tsec-http4s" % "0.4.0",
      "io.github.kirill5k" %% "mongo4cats-circe" % mongo4catsVersion,
      "io.github.kirill5k" %% "mongo4cats-core" % mongo4catsVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.scalatest" %% "scalatest" % "3.2.10" % Test,
      "org.typelevel" %% "cats-core" % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.2.9",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.3.0" % Test,
      "org.typelevel" %% "log4cats-slf4j" % "2.1.1"
    ) ++ Seq(
      // scala 2 dependencies
      "com.github.pureconfig" %% "pureconfig" % "0.17.0",
      "com.lihaoyi" %% "scalatags" % "0.10.0"
    ).map(_.cross(CrossVersion.for3Use2_13)),
    // plugin configurations
    mainClass in (Compile, run) := Some(
      "com.github.karlchan.beatthequeue.Main"
    ),
    reStart / mainClass := Some(
      "com.github.karlchan.beatthequeue.server.Server"
    ),
    Revolver.enableDebugging()
  )
  .enablePlugins(JavaAppPackaging)
