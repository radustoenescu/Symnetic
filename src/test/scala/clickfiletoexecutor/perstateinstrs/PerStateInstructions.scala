package clickfiletoexecutor.perstateinstrs

import org.change.v2.analysis.processingmodels.instructions.{Forward, Fail}
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext
import org.scalatest.{Matchers, FlatSpec}
import org.change.v2.analysis.processingmodels.State

/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
class PerStateInstructions extends FlatSpec with Matchers {

  "Per state instructions" should "be executed and disappear" in {
    val ctx = ClickExecutionContext.clean(
      instructions = Map(
        "a" -> Forward("b")
      ),
      okStates = List(
        State.bigBang.forwardTo("a").addPerStateInstruction("b" -> Fail("Superb fail"))
      )
    )

    val finalCtx = ctx.runUntilDone

    finalCtx.failedStates should have length 1
  }

}
