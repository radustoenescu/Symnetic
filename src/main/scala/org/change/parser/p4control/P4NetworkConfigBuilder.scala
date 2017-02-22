
package org.change.parser.p4control

import org.antlr.v4.runtime._;
import org.antlr.v4.runtime.tree._;
import generated.p4control.P4BaseListener
import generated.p4control.P4Parser
import generated.p4control.P4Parser._
import org.change.v2.abstractnet.generic._
import org.change.v2.abstractnet.generic.NetworkConfig
import org.change.v2.abstractnet.generic.GenericElement

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.ArrayBuffer

class P4NetworkConfigBuilder(val configName: Option[String] = None) extends P4BaseListener {

  override def enterP4(ctx: P4Context) {
    println("Enter P4");
  }

  override def exitP4(ctx: P4Context)  {
    println("Exit P4");
  }

  override def enterControl_fn_name(ctx: Control_fn_nameContext) {
    println("Enter control fn");
  }

  override def exitControl_fn_name(ctx:Control_fn_nameContext) {
    println("Exit control fn");
  }

  override def enterControl_block(ctx:Control_blockContext) { }

  override def exitControl_block(ctx:Control_blockContext) {
    println("Exit control block " + ctx.getText());
  }

  override def enterControl_statement(ctx:Control_statementContext) { }

  override def enterApply_table_call(ctx:Apply_table_callContext) { }

  override def exitApply_table_call(ctx:Apply_table_callContext) { }

  override def enterTable_name(ctx:Table_nameContext) { }

  override def exitTable_name(ctx:Table_nameContext) { }

  override def enterApply_and_select_block(ctx:Apply_and_select_blockContext) { }

  override def exitApply_and_select_block(ctx:Apply_and_select_blockContext) { }

  override def enterCase_list(ctx:Case_listContext) { }

  override def exitCase_list(ctx:Case_listContext) { }

  override def enterAction_case(ctx:Action_caseContext) { }

  override def exitAction_case(ctx:Action_caseContext) { }

  override def enterAction_or_default(ctx:Action_or_defaultContext) { }

  override def exitAction_or_default(ctx:Action_or_defaultContext) { }

  override def enterAction_name(ctx:Action_nameContext) { }

  override def exitAction_name(ctx:Action_nameContext) { }

  override def enterHit_miss_case(ctx:Hit_miss_caseContext) { }

  override def exitHit_miss_case(ctx:Hit_miss_caseContext) { }

  override def enterHit_or_miss(ctx:Hit_or_missContext) { }

  override def exitHit_or_miss(ctx:Hit_or_missContext) { }

  override def enterIf_else_statement(ctx:If_else_statementContext) {
    println("Enter ifelse statement");
  }

  override def exitIf_else_statement(ctx:If_else_statementContext) {
    println("Exit ifelse statement");
  }

  override def enterElse_block(ctx:Else_blockContext) { }

  override def exitElse_block(ctx:Else_blockContext) { }

  override def enterBool_expr(ctx:Bool_exprContext) { }

  override def exitBool_expr(ctx:Bool_exprContext) {
    println("Matched bool expr rule "+ctx.getText());
  }

  override def enterExp(ctx:ExpContext) { }

  override def exitExp(ctx:ExpContext) {
    println("Matched exp rule "+ctx.getRuleIndex()+ " " + ctx.getText());
  }

  override def enterBin_op(ctx:Bin_opContext) { }
  override def exitBin_op(ctx:Bin_opContext) { println("Binary operation:");}

  override def enterUn_op(ctx:Un_opContext) { }

  override def exitUn_op(ctx:Un_opContext) { }

  override def enterBool_op(ctx:Bool_opContext) { }

  override def exitBool_op(ctx:Bool_opContext) {
    println("Matched bool op "+ ctx.getText());
  }

  override def enterRel_op(ctx:Rel_opContext) { }

  override def exitRel_op(ctx:Rel_opContext) {
    println("Matched relop value "+ctx.getText());
  }

  override def enterHeader_ref(ctx:Header_refContext) { }

  override def exitHeader_ref(ctx:Header_refContext) {
    println("Matched header ref " + ctx.getText());
  }

  override def enterInstance_name(ctx:Instance_nameContext) { }

  override def exitInstance_name(ctx:Instance_nameContext) { }

  override def enterIndex(ctx:IndexContext) { }
  
  override def exitIndex(ctx:IndexContext) { }
  
  override def enterField_ref(ctx:Field_refContext) { }

  override def exitField_ref(ctx:Field_refContext) {
    println("Matched field ref "+ctx.getText());
  }

  override def enterField_name(ctx:Field_nameContext) { }

  override def exitField_name(ctx:Field_nameContext) {
    println("Matched field name "+ctx.getText());
  }

  override def enterConst_value(ctx:Const_valueContext) { }

  override def exitConst_value(ctx:Const_valueContext) {
    println("Matched const value "+ctx.getText());
  }

  override def enterComment(ctx:CommentContext) { }

  override def exitComment(ctx:CommentContext) { }

  override def enterEveryRule(ctx:ParserRuleContext) { }
  override def exitEveryRule(ctx:ParserRuleContext) { }

  override def visitTerminal(node: TerminalNode) { }

  override def visitErrorNode(node:ErrorNode) { }

  private val elements = new ArrayBuffer[GenericElement]()
  private var pathBuilder: ArrayBuffer[PathComponent] = _
  private val foundPaths = ArrayBuffer[List[PathComponent]]()

  def buildNetworkConfig() = new NetworkConfig(configName, elements.map(element => (element.name, element)).toMap, foundPaths.toList)


  private def buildElementName(elementName: String): String =  elementName

}
