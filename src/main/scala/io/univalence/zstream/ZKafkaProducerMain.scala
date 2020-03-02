package io.univalence.zstream

import java.io.{File, FileInputStream, IOException}
import org.apache.kafka.clients.producer.ProducerRecord
import zio.ZIO
import zio.blocking.Blocking
import zio.duration.Duration
import zio.kafka.client.serde.Serde
import zio.kafka.client.{Producer, ProducerSettings}
import zio.stream.{ZSink, ZStream, ZStreamChunk}

object ZKafkaProducerMain extends zio.App {
  val bootstrapServers = List("localhost:9092")

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val settings = ProducerSettings(
      bootstrapServers = bootstrapServers,
      closeTimeout = Duration.Zero,
      extraDriverSettings = Map()
    )

    Producer
      .make[Any, String, String](settings, Serde.string, Serde.string)
      .use { producer => program(producer) }
      .fold(f => { f.printStackTrace(); 1 }, _ => 0)
  }

  def program(producer: Producer[Any, String, String]): ZIO[Blocking, Throwable, Unit] = {
    val stream = open("src/main/resources/data.flf")
    for {
      _ <- stream.chunks
        .aggregate(ZSink.utf8DecodeChunk)
        .aggregate(ZSink.splitLines)
        .mapConcatChunk(identity)
        .foreach(line => producer.produce(new ProducerRecord[String, String]("user-data", line)))
    } yield ()
  }

  def open(filename: String): ZStreamChunk[Any, IOException, Byte] = {
    val file = new File(filename)
    val fis  = new FileInputStream(file)

    ZStream.fromInputStream(fis)
  }

}
