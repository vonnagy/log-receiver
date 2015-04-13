logLevel := Level.Warn
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")