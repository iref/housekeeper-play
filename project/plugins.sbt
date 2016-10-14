resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play"       % "sbt-plugin"           % "2.5.9")
addSbtPlugin("com.typesafe.sbt"        % "sbt-digest"           % "1.1.0")
addSbtPlugin("org.scalariform"         % "sbt-scalariform"      % "1.6.0")
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat"        % "1.0.4")
addSbtPlugin("com.timushev.sbt"        % "sbt-updates"          % "0.1.8")
addSbtPlugin("net.virtual-void"        % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("org.scoverage"           % "sbt-scoverage"        % "1.3.5")