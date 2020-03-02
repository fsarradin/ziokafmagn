package io.univalence.zstream

import java.io.{File, FileInputStream, IOException}
import zio.ZIO
import zio.console.putStrLn
import zio.duration._
import zio.kafka.client._
import zio.kafka.client.serde._
import zio.stream._

object ZKafkaConsumerMain extends zio.App {

  import FromFixedLengthData._
  import implicits._

  val bootstrapServers = List("127.0.0.1:9092")

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val settings =
      ConsumerSettings(
        bootstrapServers = bootstrapServers,
        groupId = "user-consumer",
        clientId = "zkafka",
        closeTimeout = Duration.Zero,
        extraDriverSettings = Map(),
        pollInterval = 250.millis,
        pollTimeout = 50.millis,
        perPartitionChunkPrefetch = 2
      )
    val subscription = Subscription.topics("user-data")

    // TODO doesn't seem to work, rely on ConsumerWIP for incremental construction
    Consumer
      .make(settings)
      .use { consumer =>
        val program =
          for {
            data <- consumer
              .subscribeAnd(subscription)
              .plainStream[Any, String, String](Serde.string, Serde.string)
              .tap(record => putStrLn(s"@${record.offset}: ${record.value}"))
              .map(record => parse(record.value))
              .fold(Map.empty[UserId, CompleteUser]) {
                case (m, None) => m
                case (m, Some(Left(u))) =>
                  val user = CompleteUser(u.id, u.name, u.age, None)
                  m.updated(u.id, user)
                case (m, Some(Right(a))) =>
                  m.updated(a.userId, m(a.userId).copy(address = Some(a)))
              }
            _ <- putStrLn(data.toString())
          } yield ()

        program
      }
      .fold({ f => f.printStackTrace(); 1 }, _ => 0)
  }

  type ParseResult = Option[Either[User, Address]]

  def parse(line: String): ParseResult = {
    val discr = line.splitAt(1)._1
    if (discr == "D")
      Some(Left(fromFixedLength[User](line)))
    else if (discr == "A")
      Some(Right(fromFixedLength[Address](line)))
    else None
  }

  def open(filename: String): ZStreamChunk[Any, IOException, Byte] = {
    val file = new File(filename)
    val fis  = new FileInputStream(file)

    ZStream.fromInputStream(fis)
  }

}
