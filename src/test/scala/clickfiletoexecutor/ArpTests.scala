package clickfiletoexecutor

import org.change.parser.clickfile.ClickToAbstractNetwork
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext
import org.scalatest.{Matchers, FlatSpec}

/**
  * Created by tudalex on 20.01.2016.
  */
class ArpTests extends FlatSpec with Matchers {
    "A arp " should "be correctly solved " in {
      val absNet = ClickToAbstractNetwork.buildConfig("src/main/resources/click_test_files/ARP.click")
      val executor = ClickExecutionContext.fromSingle(absNet, initialIsClean = false)

      var crtExecutor = executor
      while(! crtExecutor.isDone) {
        crtExecutor = crtExecutor.execute()
      }

      crtExecutor.failedStates should have length (0)
      crtExecutor.stuckStates should have length (3)
      crtExecutor.okStates should have length (0)
      crtExecutor.stuckStates(0).history.head should be ("dst-out")
      crtExecutor.stuckStates(1).history.head should be ("dst-out")
      crtExecutor.stuckStates(2).history.head should be ("dst-out")
    }

  "A arp classifier " should " classify correctly " in {
      val absNet = ClickToAbstractNetwork.buildConfig("src/main/resources/click_test_files/CheckARPHeader.click")
      val executor = ClickExecutionContext.fromSingle(absNet)

      var crtExecutor = executor
      while(! crtExecutor.isDone) {
        crtExecutor = crtExecutor.execute()
      }

      crtExecutor.failedStates should have length (0)
      crtExecutor.stuckStates should have length (2)
      crtExecutor.stuckStates(0).history.head should be ("dst-out")
      crtExecutor.stuckStates(1).history.head should be ("dst-out")
      crtExecutor.okStates should have length (0)
  }

}
