package org.change.v2.executor.clickabstractnetwork

import org.change.symbolicexec.verification.Rule
import org.change.v2.abstractnet.generic.NetworkConfig
import org.change.v2.analysis.processingmodels.instructions.InstructionBlock
import org.change.v2.analysis.processingmodels.{LocationId, Instruction, State}
import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext.{InvariantCheck, SnapshotInstruction}
import org.change.v2.executor.clickabstractnetwork.verificator.PathLocation

import scala.annotation.tailrec

/**
  * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 *
 * An execution context is determined by the instructions it can execute and
 * a set of states that were explored.
 *
 * A port is an Int, that maps to an instruction.
 *
 */
class ClickExecutionContext(
   val instructions: Map[LocationId, Instruction],
   val links: Map[LocationId, LocationId],
   val okStates: List[State],
   val failedStates: List[State],
   val stuckStates: List[State],
   val checkInstructions: Map[LocationId, Instruction] = Map.empty,
   val checkInvariantInstructions: Map[LocationId, (SnapshotInstruction, InvariantCheck)] = Map.empty
) {

  def +(that: ClickExecutionContext) = new ClickExecutionContext(
    this.instructions ++ that.instructions,
    this.links ++ that.links,
    this.okStates ++ that.okStates,
    this.failedStates ++ that.failedStates,
    this.stuckStates ++ that.stuckStates,
    this.checkInstructions ++ that.checkInstructions
  )

  @tailrec final def runUntilDone: ClickExecutionContext = if (isDone) this else execute().runUntilDone

  def isDone: Boolean = okStates.isEmpty

  def execute(verbose: Boolean = false): ClickExecutionContext = {
    val (ok, fail, stuck) = (for {
      sPrime <- okStates
      s = if (links contains sPrime.location)
          sPrime.forwardTo(links(sPrime.location))
        else
          sPrime
      stateLocation = s.location
    } yield {
        if (instructions contains stateLocation) {
//          Apply instructions
          val afterProcessing = instructions(stateLocation)(s, verbose)

//          Install invariant instructions

//          Apply check instructions on output ports
          val (needChecks, needNotChecks) = afterProcessing._1.partition(s => checkInstructions.contains(s.location))
          val afterChecks = needChecks.map(s => checkInstructions(s.location)(s,verbose)).unzip

          val okAfterChecks = needNotChecks ++ afterChecks._1.flatten
          val failedAfterChecks = afterProcessing._2 ++ afterChecks._2.flatten

//          Apply invariant checks
          val (okAfterInvs, failedAfterInvs) = okAfterChecks.map(_.executePerStateInstructions(verbose)).unzip

          (okAfterInvs.flatten, failedAfterChecks ++ failedAfterInvs.flatten, Nil)
        } else
          (Nil, Nil, List(s))
      }).unzip3

      new ClickExecutionContext(instructions,
        links,
        ok.flatten,
        failedStates ++ fail.flatten,
        stuckStates ++ stuck.flatten,
        checkInstructions
      )
  }

  private def verboselyStringifyStates(ss: List[State]): String = ss.zipWithIndex.map( si =>
    "State #" + si._2 + "\n\n" + si._1.instructionHistory.reverse.mkString("\n") + "\n\n" + si._1.toString)
    .mkString("\n")

  // TODO: MOve this elsewhere, and allow some sort of customization.
  private def verboselyStringifyStatesWithExample(ss: List[State]): String = ss.zipWithIndex.map( si =>
    "State #" + si._2 + "\n\n" + si._1.instructionHistory.reverse.mkString("\n") + "\n\n" + si._1.memory.verboseToString)
    .mkString("\n")

  def stringifyStates(includeOk: Boolean = true, includeStuck: Boolean = true, includeFailed: Boolean= true) = {
    (if (includeOk)
      s"Ok states (${okStates.length}):\n" + verboselyStringifyStates(okStates)
    else
      "") +
    (if (includeStuck)
      s"Stuck states (${stuckStates.length}):\n" + verboselyStringifyStates(stuckStates)
    else
      "") +
    (if (includeFailed)
      s"Failed states (${failedStates.length}): \n" + verboselyStringifyStates(failedStates)
    else
      "")
  }

  def concretizeStates: String = (stuckStates ++ okStates).map(_.memory.concretizeSymbols).mkString("\n----------\n")

  /**
   * TODO: This stringification should stay in a different place.
   * @param includeOk
   * @param includeStuck
   * @param includeFailed
   * @return
   */
  def verboselyStringifyStates(includeOk: Boolean = true, includeStuck: Boolean = true, includeFailed: Boolean= true) = {
    (if (includeOk)
      s"Ok states (${okStates.length}):\n" + verboselyStringifyStatesWithExample(okStates)
    else
      "") +
      (if (includeStuck)
        s"Stuck states (${stuckStates.length}):\n" + verboselyStringifyStatesWithExample(stuckStates)
      else
        "") +
      (if (includeFailed)
        s"Failed states (${failedStates.length}): \n" + verboselyStringifyStatesWithExample(failedStates)
      else
        "")
  }
}

object ClickExecutionContext {

  def clean(
    instructions: Map[LocationId, Instruction] = Map.empty,
    links: Map[LocationId, LocationId] = Map.empty,
    okStates: List[State] = Nil,
    failedStates: List[State] = Nil,
    stuckStates: List[State] = Nil,
    checkInstructions: Map[LocationId, Instruction] = Map.empty,
    checkInvariantInstructions: Map[LocationId, (SnapshotInstruction, InvariantCheck)] = Map.empty
  ): ClickExecutionContext = new ClickExecutionContext(
    instructions,
    links,
    okStates,
    failedStates,
    stuckStates,
    checkInstructions,
    checkInvariantInstructions
  )

  type SnapshotInstruction = Instruction
  type CheckSnapshotInstruction = Instruction
  type InvariantCheck = (LocationId, CheckSnapshotInstruction)

  /**
   * Builds a symbolic execution context out of a single click config file.
   *
   * @param networkModel
   * @param verificationConditions
   * @param includeBigBang
   * @return
   */
  def fromSingle( networkModel: NetworkConfig,
                  verificationConditions: List[List[Rule]] = Nil,
                  includeBigBang: Boolean = true): ClickExecutionContext = {
    // Collect instructions for every element.
    val instructions = networkModel.elements.values.foldLeft(Map[LocationId, Instruction]())(_ ++ _.instructions)
    // Collect check instructions corresponding to network rules.
    val checkInstructions = verificationConditions.flatten.map( r => {
        networkModel.elements(r.where.element).outputPortName(r.where.port) -> InstructionBlock(r.whatTraffic)
      }).toMap
    // Create forwarding links.
    val links = networkModel.paths.flatMap( _.sliding(2).map(pcp => {
      val src = pcp.head
      val dst = pcp.last
      networkModel.elements(src._1).outputPortName(src._3) -> networkModel.elements(dst._1).inputPortName(dst._2)
    })).toMap
    // TODO: This should be configurable.
    val initialStates = if (includeBigBang) List(State.bigBang.forwardTo(networkModel.entryLocationId)) else Nil

    new ClickExecutionContext(instructions, links, initialStates, Nil, Nil, checkInstructions)
  }

  def buildAggregated(
            configs: Iterable[NetworkConfig],
            interClickLinks: Iterable[(String, String, Int, String, String, Int)],
            verificationConditions: List[List[Rule]] = Nil): ClickExecutionContext = {
    // Create a context for every network config.
    val ctxes = configs.map(ClickExecutionContext.fromSingle(_, includeBigBang = false))
    // Keep the configs for name resolution.
    val configMap: Map[String, NetworkConfig] = configs.map(c => c.id.get -> c).toMap
    // Add forwarding links between click files.
    val links = interClickLinks.map(l => {
      val ela = l._1 + "-" + l._2
      val elb = l._4 + "-" + l._5
      configMap(l._1).elements(ela).outputPortName(l._3) -> configMap(l._4).elements(elb).inputPortName(l._6)
    }).toMap
    // Collect check instructions corresponding to network rules.
    val checkInstructions = verificationConditions.flatten.map( r => {
      val elementName = r.where.vm + "-" + r.where.element
      configMap(r.where.vm).elements(elementName).outputPortName(r.where.port) -> InstructionBlock(r.whatTraffic)
    }).toMap
    // Build the unified execution context.
    ctxes.foldLeft(new ClickExecutionContext(
      Map.empty,
      links,
      // TODO: Should be configurable
      List(State.bigBang.forwardTo(configs.head.entryLocationId)),
      Nil,
      Nil,
      checkInstructions
    ))(_ + _)
  }

  def createWitoutVmPrefix(networkModel: NetworkConfig, verificationConditions: List[Rule] = Nil): ClickExecutionContext =
    apply(networkModel, verificationConditions)

  def createWithPrefix(networkModel: NetworkConfig, verificationConditions: List[Rule] = Nil): ClickExecutionContext = {
    val instructions = networkModel.elements.values.foldLeft(Map[LocationId, Instruction]())(_ ++ _.instructions.map(
      ipi => {
        networkModel.id + ipi._1 -> ipi._2
      }
    ))
    val checkInstructions = verificationConditions.map( r => {
      networkModel.elements(r.where.element).outputPortName(r.where.port) -> InstructionBlock(r.whatTraffic)
    }).toMap

    val links = networkModel.paths.flatMap( _.sliding(2).map(pcp => {
      val src = pcp.head
      val dst = pcp.last
      networkModel.elements(src._1).outputPortName(src._3) -> networkModel.elements(dst._1).inputPortName(dst._2)
    })).toMap

    val initialState = State.bigBang.forwardTo(networkModel.entryLocationId)

    new ClickExecutionContext(instructions, links, List(initialState), Nil, Nil, checkInstructions)
  }
}
