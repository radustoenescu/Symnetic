package org.change.v2

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete.{SymbolicValue, ConstantValue}
import org.change.v2.analysis.expression.concrete.nonprimitive.:@
import org.change.v2.analysis.memory._
import org.change.v2.analysis.memory.TagExp._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.analysis.processingmodels.{instructions, Instruction, LocationId}
import org.change.v2.util.conversion.RepresentationConversion._
import org.change.v2.util.canonicalnames._;


class ARPQuerier(name: String,
                   elementType: String,
                   inputPorts: List[Port],
                   outputPorts: List[Port],
                   configParams: List[ConfigParameter])
  extends GenericElement(name,
    elementType,
    inputPorts,
    outputPorts,
    configParams) {

  /**
    * Every Click element modeled in Symnet has an "instructions" map.
    *
    * This maps every input port (identified by a LocationId) to the instruction performed.
    *
    * Knowing the input port id, one can get its corresponding LocationId by calling "inputPortName()".
    * The same holds true for output port names (used usually in conjuction with the "Forward instruction").
    *
    * Usually the sequence of instructions performed on a given input port ends with a "Forward". This
    * ensures the state gets propagated further, otherwise it becomes stuck (there are no instructions
    * to be executed).
    *
    * One can use the parameters provided in the Click file by issuing "configParams(id).value" where "id"
    * is the 0-based index of the parameter. For instance: "ipToNumber( configParams(0).value )" assumes the
    * first parameter of this click element is an IPv4 address and converts it to a long value.
    * @return
    */
  val ARPL3 = -1000
  val ARPL2 = ARPL3 - 144
  def genCase(configParams: List[ConfigParameter]): InstructionBlock = {
    var t = InstructionBlock(
      CreateTag("L2", ARPL2),
      Allocate(EtherDst, 48),
      Assign(EtherDst, SymbolicValue()),

      Allocate(EtherSrc, 48),
      Assign(EtherSrc, SymbolicValue()),

      Allocate(EtherType, 16),
      Assign(EtherType, ConstantValue(EtherProtoARP)),

      CreateTag("L3", ARPL3),
      //L3
      Allocate(ARPHWAddrSize, 8),
      Assign(ARPHWAddrSize, ConstantValue(6)),

      Allocate(ARPProtoAddrSize, 8),
      Assign(ARPProtoAddrSize, ConstantValue(4)),

      Allocate(ARPOpCode, 16),
      Assign(ARPOpCode, ConstantValue(1)),

      // This should be constant value
      Allocate(ARPHWSender, 48),
      Assign(ARPHWSender, SymbolicValue()),

      // This should be fixed to IP value
      Allocate(ARPProtoSender, 32),
      Assign(ARPProtoSender, ConstantValue(EtherProtoIP)),

      Allocate(ARPHWReceiver, 48),
      Assign(ARPHWReceiver, :@(0+IPDstOffset)),

      Allocate(ARPProtoReceiver, 32),
      Assign(ARPProtoReceiver, SymbolicValue()),
      Forward(outputPortName(0))
    )
     for (a <- configParams.grouped(2)) {
       val ip = ipToNumber(a(0).value)
       val mac = macToNumber(a(1).value)
       t = InstructionBlock(
         If(
           Constrain(IPDst, :==:(ConstantValue(ip))),
           InstructionBlock(
             Assign(EtherDst, ConstantValue(mac)),
             Forward(outputPortName(1))),
           t
         )
       )

     }
    t
  }
  override def instructions: Map[LocationId, Instruction] = Map(
    inputPortName(0) ->
      InstructionBlock(
          genCase(configParams)
      ),
    inputPortName(1) ->
      InstructionBlock(
        // Checking that we have an arp packet
        Constrain(EtherType, :==:(ConstantValue(EtherProtoARP))),
        // Check that this is an ARP Response
        Constrain(ARPOpCode, :==:(ConstantValue(ARPOpCodeResponse))),
        CreateTag("L3", 0),
        CreateTag("L2", Tag("L3") - 144),
        Assign(EtherDst, :@(ARPL3 + 368)),
        Forward(outputPortName(1))

      )


  )

  override def outputPortName(which: Int = 0): String = s"$name-$which-out"
  override def inputPortName(which: Int = 0): String = s"$name-$which-in"
}

class ARPQuerierElementBuilder(name: String, elementType: String)
  extends ElementBuilder(name, elementType) {

  override def buildElement: GenericElement = {
    new ARPQuerier(name, elementType, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object ARPQuerier {
  private var unnamedCount = 0

  private val genericElementName = "arpquerier"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): ARPQuerierElementBuilder = {
    increment;
    new ARPQuerierElementBuilder(name, "ARPQuerier")
  }

  def getBuilder: ARPQuerierElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
