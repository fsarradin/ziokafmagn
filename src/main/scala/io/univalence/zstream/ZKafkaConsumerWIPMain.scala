package io.univalence.zstream

import zio._
import zio.console._
import zio.duration._
import zio.kafka.client._
import zio.kafka.client.serde._

object ZKafkaConsumerWIPMain extends zio.App {
  val bootstrapServers = List("127.0.0.1:9092")

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
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

    val program =
      Consumer
        .make(settings)
        .use { consumer =>
          consumer
            .subscribeAnd(subscription)
            .plainStream(Serde.string, Serde.string)
            .map(record => ZKafkaConsumerMain.parse(record.value))
            .foreach(value => putStrLn(s"value: ${value}"))
        }
//      Consumer.consumeWith(
//        settings = settings,
//        subscription = subscription,
//        keyDeserializer = Serde.string,
//        valueDeserializer = Serde.string
//      ) { (k, v) => putStrLn(s"value: $v") }

    program.fold(f => { f.printStackTrace(); 1 }, _ => 0)
  }
}
