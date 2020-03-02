package io.univalence.zstream

import scala.language.experimental.macros

trait FixedLengthField {
  val offset: Int
  val length: Int
  lazy val endOffset: Int = offset + length
  //  def from(line: String): this.type
}

trait FromFixedLengthData[A] {
  def fromFixedLength(line: String): A
}
object FromFixedLengthData {

  import magnolia._

  @inline def apply[A](
      implicit ev: FromFixedLengthData[A]
  ): FromFixedLengthData[A] = ev

  type Typeclass[A] = FromFixedLengthData[A]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] =
    line => ctx.construct { p => p.typeclass.fromFixedLength(line) }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]

  def fromFixedLength[A: FromFixedLengthData](line: String): A =
    FromFixedLengthData[A].fromFixedLength(line)

}
