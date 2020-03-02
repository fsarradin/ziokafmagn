package io.univalence.zstream

case class UserId(value: String)
object UserId extends FixedLengthField {
  override val offset: Int = 1
  override val length: Int = 5
  def from(line: String): UserId =
    UserId(line.substring(UserId.offset, UserId.endOffset))
}

case class UserName(value: String)
object UserName extends FixedLengthField {
  override val offset: Int = 6
  override val length: Int = 10
  def from(line: String): UserName =
    UserName(line.substring(UserName.offset, UserName.endOffset).trim)
}

case class UserAge(value: Int)
object UserAge extends FixedLengthField {
  override val offset: Int = 16
  override val length: Int = 3
  def from(line: String): UserAge =
    UserAge(line.substring(UserAge.offset, UserAge.endOffset).trim.toInt)
}

case class User(id: UserId, name: UserName, age: UserAge)

case class AddressStreet(value: String)
object AddressStreet extends FixedLengthField {
  override val offset: Int = 6
  override val length: Int = 20
  def from(line: String): AddressStreet =
    AddressStreet(
      line.substring(AddressStreet.offset, AddressStreet.endOffset).trim
    )
}

case class AddressPostCode(value: String)
object AddressPostCode extends FixedLengthField {
  override val offset: Int = 26
  override val length: Int = 5
  def from(line: String): AddressPostCode =
    AddressPostCode(
      line.substring(AddressPostCode.offset, AddressPostCode.endOffset).trim
    )
}

case class AddressCity(value: String)
object AddressCity extends FixedLengthField {
  override val offset: Int = 31
  override val length: Int = 20
  def from(line: String): AddressCity =
    AddressCity(line.substring(AddressCity.offset, AddressCity.endOffset).trim)
}

case class Address(
    userId: UserId,
    street: AddressStreet,
    postCode: AddressPostCode,
    city: AddressCity
)

case class CompleteUser(id: UserId, name: UserName, age: UserAge, address: Option[Address])

object implicits {
  implicit val userIdFromLength: FromFixedLengthData[UserId]     = line => UserId.from(line)
  implicit val userNameFromLength: FromFixedLengthData[UserName] = line => UserName.from(line)
  implicit val userAgeFromLength: FromFixedLengthData[UserAge]   = line => UserAge.from(line)
  implicit val addressStreetFromLength: FromFixedLengthData[AddressStreet] = line =>
    AddressStreet.from(line)
  implicit val addressPostCodeFromLength: FromFixedLengthData[AddressPostCode] = line =>
    AddressPostCode.from(line)
  implicit val addressCityFromLength: FromFixedLengthData[AddressCity] = line =>
    AddressCity.from(line)

  def main(args: Array[String]): Unit = {
    import io.univalence.zstream.FromFixedLengthData.fromFixedLength

    val dataUser    = "D12345      John 32"
    val dataAddress = "A123455 RUE DU TEMPLES    93170BAGNOLET            "

    println(fromFixedLength[User](dataUser))
    println(fromFixedLength[Address](dataAddress))
  }
}
