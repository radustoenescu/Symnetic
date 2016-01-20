organization  := "org.change"

version       := "0.2"

scalaVersion  := "2.11.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

fork := true

libraryDependencies ++= {
  Seq(
    "org.antlr" % "antlr4" % "4.3",
    "commons-io" % "commons-io" % "2.4",
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
    "io.spray" %%  "spray-json" % "1.3.2"
  )
}

lazy val sample = taskKey[Unit]("Interpreting")

fullRunTask(sample, Compile, "org.change.v2.runners.experiments.SEFLRunner")

lazy val click = taskKey[Unit]("Symbolically running Template.click")

fullRunTask(click, Compile, "org.change.v2.runners.experiments.TemplateRunner")

lazy val arp = taskKey[Unit]("Symbolically running ARP.click")

fullRunTask(arp, Compile, "org.change.v2.runners.experiments.ARPRunner")

lazy val arpResponder = taskKey[Unit]("Symbolically running ARPResponder.click")

fullRunTask(arpResponder, Compile, "org.change.v2.runners.experiments.ARPResponderRunner")

lazy val arpQuerier = taskKey[Unit]("Symbolically running ARPQuerier.click")

fullRunTask(arpQuerier, Compile, "org.change.v2.runners.experiments.ARPQuerierRunner")

lazy val click_exampl = taskKey[Unit]("Symbolically running TemplateExampl.click with example generation")

lazy val symb = taskKey[Unit]("Symbolically running Template.click without validation")

fullRunTask(symb, Compile, "org.change.v2.runners.experiments.TemplateRunnerWithoutValidation")

lazy val mc = taskKey[Unit]("Running multiple VMs")

fullRunTask(mc, Compile, "org.change.v2.runners.experiments.MultipleVms")

lazy val fuckultatea = taskKey[Unit]("Running multiple VMs")

fullRunTask(fuckultatea, Compile, "org.change.v2.runners.experiments.MultipleVmsFacultatea")

seq(Revolver.settings: _*)
