package logreceiver.processor

import java.nio.ByteBuffer

import akka.actor.{Actor, ActorLogging}
import com.github.vonnagy.service.container.health.HealthInfo
import com.github.vonnagy.service.container.metrics.{Meter, Counter}

/**
 * Derive any processors by applying this trait and then add it to log processors
 * in the configuraiton file
 */
trait Processor extends Actor with ActorLogging {

  import context.system
  def lineMetricPrefix: String
  val lineReceivedCount = Counter(s"$lineMetricPrefix.line.receive")
  val lineReceivedMeter = Meter(s"$lineMetricPrefix.line.receive.meter")

  val appPattern = """\s(t\.[a-zA-Z0-9-]+)\s""".r

  override def preStart(): Unit = {
    log.info(s"${this.getClass.getName} starting at ${context.self.path}")
  }

  override def postStop(): Unit = {
    log.info(s"${this.getClass.getName} is stopping")
  }

  override def receive = health orElse {
    case ProcessorReady => // We are ready to go
      context.become(running orElse health)
      log.info(s"${this.getClass.getName} is ready to receive messages")
      context.parent ! ProcessorReady
    case _ =>
  }

  def health: Actor.Receive = {
    case CheckHealth => // How are we doing
      sender ! getHealth
  }

  /** Must be implemented by an Actor. */
  def running: Receive

  /** Must be implemented by an Actor. */
  def getHealth: HealthInfo

  def processPayload(load: String, data: Seq[Tuple2[ByteBuffer, String]]): Seq[Tuple2[ByteBuffer, String]] = {

    if (load.length > 0) {

      lineReceivedMeter.meter {

        val part = load.takeWhile(_ != ' ')
        val pos = part.length + 1
        val size = part.toInt
        val line = load.substring(pos, Math.min(pos + size, load.length))
        lineReceivedCount.incr

        val name = appPattern.findFirstIn(line) match {
          case Some(name) =>
            log.debug(s"${name.trim} $line")
            name.trim

          case None =>
            log.warning(s"No application name found for log: $line")
            "unknown"
        }

        val newData = data ++ Seq((ByteBuffer.wrap(line.getBytes), name))

        if (pos + size < load.length)
          processPayload(load.substring(pos + size), newData)
        else
          newData
      }
    }
    else {
      data
    }
  }
}

