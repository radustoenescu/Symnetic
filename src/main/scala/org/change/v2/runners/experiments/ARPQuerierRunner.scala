package org.change.v2.runners.experiments

import java.io.{File, FileOutputStream, PrintStream}

import org.change.parser.clickfile.ClickToAbstractNetwork
import org.change.v2.analysis.memory.State
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext

/**
 * Author: Alexandru Tudorica
 */
object ARPQuerierRunner {

  def main (args: Array[String]) {
    val clickConfig = "src/main/resources/click_test_files/ARPQuerier.click"
    val absNet = ClickToAbstractNetwork.buildConfig(clickConfig)
    val executor = ClickExecutionContext.fromSingle(absNet, initialIsClean = false, initialState = State.bigBangARP)


    println(
      executor.instructions.mkString("\n")
    )

    var crtExecutor = executor
    while (!crtExecutor.isDone) {
      crtExecutor = crtExecutor.execute(verbose = true)
    }

    val outputFileName = "arp-query.output"
    val output = new PrintStream(new FileOutputStream(new File(outputFileName)))
    output.println(crtExecutor.concretizeStates)
    output.close()
    println("Done. Output @ " + outputFileName)
  }
}
