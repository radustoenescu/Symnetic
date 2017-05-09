package org.change.v2.runners.experiments

import java.io.{File, FileOutputStream, PrintStream, FileInputStream}

import org.change.parser.p4control._


/**
 * Author: Costin Raiciu
 * 
 */
object P4ControlRunner {

  def main (args: Array[String]) {
    val p4file = args(0);//"src/main/resources/p4_test_files/control.p4"
    val absNet = P4ToAbstractNetwork.buildConfig(new FileInputStream(p4file),p4file)
  }
}
