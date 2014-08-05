package org.change.symbolicexec

class NumericType extends Type {
  override def name = "GenericNumeric"
  def min: Long = 0
  def max: Long = Long.MaxValue
  def admissibleRange: Interval = (min, max)
  def admissibleSet: ValueSet = List(admissibleRange)
}

object NumericType {
  private lazy val instance = new NumericType

  def apply() = instance
}