package logreceiver.processor.kinesis

import java.util.concurrent.TimeUnit

import com.github.vonnagy.service.container.health.{HealthInfo, HealthState}
import com.github.vonnagy.service.container.metrics.Counter
import io.github.cloudify.scala.aws.auth.CredentialsProvider.DefaultHomePropertiesFile
import io.github.cloudify.scala.aws.kinesis.Client
import logreceiver.processor.{LogBatch, Processor}

import scala.util.Try

/**
 * Created by ivannagy on 4/10/15.
 */
class KinesisProcessor extends Processor {

  import context.system

  val endpoint = context.system.settings.config.getString("log.processors.kinesis.endpoint")
  val region = context.system.settings.config.getString("log.processors.kinesis.region")
  val accessKey = context.system.settings.config.getString("log.processors.kinesis.access-key")
  val accessSecret = context.system.settings.config.getString("log.processors.kinesis.access-secret")
  val timeout = context.system.settings.config.getDuration("log.processors.kinesis.timeout", TimeUnit.MILLISECONDS).toInt

  var connected = false

  implicit val kinesisClient = Client.fromCredentials(accessKey, accessSecret, endpoint)
  lazy val streams = verifyStreams()

  val batchReceivedCount = Counter("processors.kinesis.recieve")

  override def preStart() {
    super.preStart
    // Make sure everthing is setup properly
    verifyStreams
  }

  override def postStop() {

    Try({
      log.info("Kinesis processor stopping: {}", context.self.path)
      connected = false
    }) recover {
      case e =>
      // TODO log.error(e, "Unable to close connection to graphite at {}:{}", host, port)
    }

    super.postStop
  }

  def running: Receive = {
    // Handle the batch
    case LogBatch(token, frameId, count, payload) =>
      batchReceivedCount.incr
    //if (connected) graphite.send(name, value.toString, System.currentTimeMillis / 1000)
  }

  def getHealth: HealthInfo = connected match {
    case true =>
      new HealthInfo("kinesis", HealthState.OK, s"The processor running and attached to kinesis at: $endpoint:$region")
    case false =>
      new HealthInfo("kinesis", HealthState.DEGRADED, s"The processor is running, but can't attach to kinesis at:  $endpoint:$region")
  }

  /**
   * Make sure the the proper streams are up and running before registering or accepting any log work
   */
  def verifyStreams(): Map[String, StreamManager] = {

    log.info("Locating the streams {} and {}", "log-stream", "metric-stream")
    Map(("log-stream", new StreamManager("log-stream")), ("metric-stream", new StreamManager("metric-stream")))

  }
}
