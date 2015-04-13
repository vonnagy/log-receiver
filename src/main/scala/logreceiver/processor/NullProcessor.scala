package logreceiver.processor

import java.nio.ByteBuffer

import com.github.vonnagy.service.container.health.{HealthState, HealthInfo}
import com.github.vonnagy.service.container.metrics.Counter

/**
 * Created by ivannagy on 4/13/15.
 */
class NullProcessor extends Processor {

  import context.system
  val batchReceivedCount = Counter("processors.null.batch.receive")
  val lineReceivedCount = Counter("processors.null.line.receive")

  override def preStart() {
    super.preStart
    self ! ProcessorReady
  }

  override def running: Receive = {
    case LogBatch(token, frameId, count, payload) =>
      batchReceivedCount.incr
      processPayload(payload, lineReceivedCount, Seq[Tuple2[ByteBuffer, String]]()).foreach { ent =>
        println(ent)
      }

  }

  override def getHealth: HealthInfo = {
    new HealthInfo("null-processor", HealthState.OK, s"The processor running")
  }
}
