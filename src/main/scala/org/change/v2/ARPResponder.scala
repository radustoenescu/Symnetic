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


class ARPResponder(name: String,
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
  def genCase(configParams: List[ConfigParameter]) : InstructionBlock = {
    var t = InstructionBlock(
      Forward(outputPortName(0))
    )
    for (x <- configParams.grouped(2)) {
      val ipAndMask = x(0).value.split("/")
      val ip = ipAndMask(0)
      val mask = ipAndMask(1)
      val range = ipAndMaskToInterval(ip, mask)
      val mac = macToNumber(x(1).value)
      t = InstructionBlock(
        If(
          Constrain(ARPProtoReceiver, :&:(:>:(ConstantValue(range._1)), :<:(ConstantValue(range._2)))),
          InstructionBlock(
            Assign(ARPHWReceiver, ConstantValue(mac)),
            // Setting the packet to be an arp reply
            Assign(ARPOpCode, ConstantValue(ARPOpCodeResponse)),
            Forward(outputPortName(1))
          ),
          t
        )
      )
    }
    t
  }

  override def instructions: Map[LocationId, Instruction] = Map(
    inputPortName(0) ->
      InstructionBlock(
              // Checking that we have an arp packet
          Constrain(EtherType, :==:(ConstantValue(EtherProtoARP))),
          // Check that this is an ARP Request
          Constrain(ARPOpCode, :==:(ConstantValue(ARPOpCodeRequest))),
          genCase(configParams)
      )
  )

  override def outputPortName(which: Int = 0): String = s"$name-$which-out"
}

class ARPResponderElementBuilder(name: String, elementType: String)
  extends ElementBuilder(name, elementType) {

  override def buildElement: GenericElement = {
    new ARPResponder(name, elementType, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object ARPResponder {
  private var unnamedCount = 0

  private val genericElementName = "arpresponder"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): ARPResponderElementBuilder = {
    increment;
    new ARPResponderElementBuilder(name, "ARPResponder")
  }

  def getBuilder: ARPResponderElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
