package logreceiver.processor.kinesis

import java.util.concurrent.TimeUnit

import akka.actor.ActorContext
import com.github.vonnagy.service.container.log.LoggingAdapter
import io.github.cloudify.scala.aws.kinesis.Client.ImplicitExecution._
import io.github.cloudify.scala.aws.kinesis.KinesisDsl._
import io.github.cloudify.scala.aws.kinesis.{Client, Definitions}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise, TimeoutException}
import scala.util.{Failure, Success}

/**
 * Created by ivannagy on 4/12/15.
 */
class StreamManager(name: String)(implicit context: ActorContext, client: Client) extends LoggingAdapter {

  import context.dispatcher

  val shardSize = context.system.settings.config.getInt(s"log.processors.kinesis.streams.$name.shards")
  val retries = context.system.settings.config.getInt(s"log.processors.kinesis.streams.creation-retries")
  val timeout = context.system.settings.config.getDuration(s"log.processors.kinesis.streams.activate-timout", TimeUnit.SECONDS)

  val stream = getStream

  private def getStream: Option[Definitions.Stream] = {
    log.info("Locating the stream $name")

    val p = Promise[Option[Definitions.Stream]]

    Kinesis.streams.list onComplete {
      case Failure(f) =>
        log.error("Error fetching Kinesis streams $name", f)

      case Success(streamList) =>
        // Get the filtered list of streams
        val stream = streamList.filter(s => s.equals(name))

        if (stream.size > 0) {
          log.info(s"Stream 'log-stream' already exists.")
          p success Some(Kinesis.stream(name))
        }
        else {
          p success createStream
        }

    }

    Await.result(p.future, Duration(timeout, TimeUnit.SECONDS))
  }

  def createStream(): Option[Definitions.Stream] = {

    val fut = Kinesis.streams.create(name)
    val p = Promise[Option[Definitions.Stream]]

    fut onComplete {
      case Failure(f) =>
        log.error("Error creating Kinesis streams $name", f)
        p success None
      case Success(stream) =>
        try {
          Await.result(stream.waitActive.retrying(retries), Duration(timeout, TimeUnit.SECONDS))
          p success Some(Kinesis.stream(name))
        }
        catch {
          case _: TimeoutException =>
            log.error(s"creation of stream $name timed out")
            p success None
        }
    }

    Await.result(p.future, Duration(timeout, TimeUnit.SECONDS))
  }
}
