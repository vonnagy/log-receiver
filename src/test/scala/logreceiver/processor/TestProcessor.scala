package logreceiver.processor

import com.github.vonnagy.service.container.health.{HealthInfo, HealthState}

/**
 * Created by ivannagy on 4/13/15.
 */
class TestProcessor extends Processor {

  def lineMetricPrefix = "processors.test"

  self ! ProcessorReady

  override def running: Receive = {
    case LogBatch(token, frameId, count, payload) =>
  }

  override def getHealth: HealthInfo = {
    new HealthInfo("test", HealthState.OK, s"The processor running")
  }
}
