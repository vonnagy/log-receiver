package logreceiver.processor

import java.nio.ByteBuffer

import com.github.vonnagy.service.container.health.{HealthInfo, HealthState}
import com.github.vonnagy.service.container.metrics.{Counter, Meter}

/**
 * Created by ivannagy on 4/13/15.
 */
class NullProcessor extends Processor {

  import context.system

  def lineMetricPrefix = "processors.null"
  val batchReceivedCount = Counter("processors.null.batch.receive")
  val batchReceivedMeter = Meter("processors.null.batch.receive.meter")

  override def preStart() {
    super.preStart
    self ! ProcessorReady
  }

  override def running: Receive = {
    case LogBatch(token, frameId, count, payload) =>
      batchReceivedCount.incr
      batchReceivedMeter.meter {
        processPayload(payload, Seq[Tuple2[ByteBuffer, String]]()).foreach { ent =>
          println(ent)
        }
      }
  }

  override def getHealth: HealthInfo = {
    new HealthInfo("null-processor", HealthState.OK, s"The processor running")
  }
}
