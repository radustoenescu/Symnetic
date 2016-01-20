package org.change.v2.runners.experiments

import java.io.{File, FileOutputStream, PrintStream}

import org.change.parser.clickfile.ClickToAbstractNetwork
import org.change.v2.analysis.processingmodels.State
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext

/**
 * Author: Alexandru Tudorica
 */
object ARPResponderRunner {

  def main (args: Array[String]) {
    val clickConfig = "src/main/resources/click_test_files/ARPResponder.click"
    val absNet = ClickToAbstractNetwork.buildConfig(clickConfig)
    val executor = ClickExecutionContext.fromSingle(absNet, initialIsClean = false, initialState = State.bigBangARP)

    println(
      executor.instructions.mkString("\n")
    )

    var crtExecutor = executor
    while (!crtExecutor.isDone) {
      crtExecutor = crtExecutor.execute(verbose = true)
    }

    val outputFileName = "arp-responder.output"
    val output = new PrintStream(new FileOutputStream(new File(outputFileName)))
    output.println(crtExecutor.stringifyStates())
    output.close()
    println("Done. Output @ " + outputFileName)
  }
}
