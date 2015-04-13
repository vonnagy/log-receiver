package logreceiver.processor

import com.github.vonnagy.service.container.health.{HealthState, HealthInfo}
import com.github.vonnagy.service.container.metrics.Counter

/**
 * Created by ivannagy on 4/13/15.
 */
class NullProcessor extends Processor {

  import context.system
  val batchReceivedCount = Counter("processors.null.batch.receive")
  val lineReceivedCount = Counter("processors.null.line.receive")

  val pattern = """\s(t\.[a-zA-Z0-9-]+)\s""".r

  override def preStart() {
    super.preStart
    self ! ProcessorReady
  }

  override def running: Receive = {
    case LogBatch(token, frameId, count, payload) =>
      batchReceivedCount.incr

      def process(load: String): Unit = {
        if (load.length > 0) {
          val part = load.takeWhile(_ != ' ')
          val pos = part.length + 1
          val size = part.toInt
          val line = load.substring(pos, pos + size - 1)
          lineReceivedCount.incr

          pattern.findFirstIn(line) match {
            case Some(name) => log.info(s"$name $line")
            case None => log.warning(s"No application name found for log: $line")
          }

          if (pos + size < load.length)
            process(load.substring(pos + size))
        }

      }

      process(payload)

  }

  override def getHealth: HealthInfo = {
    new HealthInfo("null-processor", HealthState.OK, s"The processor running")
  }
}
