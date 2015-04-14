package logreceiver.processor.kinesis

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import com.github.vonnagy.service.container.health.{HealthInfo, HealthState}
import com.github.vonnagy.service.container.metrics.{Counter, Meter}
import io.github.cloudify.scala.aws.kinesis.Client
import logreceiver.processor.{LogBatch, Processor, ProcessorReady}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}

/**
 * Created by ivannagy on 4/10/15.
 */
class KinesisProcessor extends Processor {

  import context.{dispatcher, system}

  val endpoint = context.system.settings.config.getString("log.processors.kinesis.endpoint")
  val accessKey = context.system.settings.config.getString("log.processors.kinesis.access-key")
  val accessSecret = context.system.settings.config.getString("log.processors.kinesis.access-secret")
  val timeout = context.system.settings.config.getDuration("log.processors.kinesis.timeout", TimeUnit.MILLISECONDS).toInt

  var connected = false

  implicit val kinesisClient = Client.fromCredentials(accessKey, accessSecret, endpoint)
  lazy val streams = verifyStreams()

  def lineMetricPrefix = "processors.kinesis"

  val batchReceivedCount = Counter("processors.kinesis.batch.receive")
  val batchReceivedMeter = Meter("processors.kinesis.batch.receive.meter")
  val failedPutCount = Counter("processors.kinesis.put-failure")

  override def preStart() {
    super.preStart
    streams
    self ! ProcessorReady
  }

  override def postStop() {

    log.info("Kinesis processor stopping: {}", context.self.path)
    connected = false

    super.postStop
  }

  def running: Receive = {
    // Handle the batch
    case b@LogBatch(token, frameId, count, payload) => processBatch(b)
  }

  def getHealth: HealthInfo = connected match {
    case true =>
      new HealthInfo("kinesis", HealthState.OK, s"The processor running and attached to kinesis")
    case false =>
      new HealthInfo("kinesis", HealthState.DEGRADED, s"The processor is running, but can't attach to kinesis")
  }

  /**
   * Make sure the the proper streams are up and running before registering or accepting any log work
   */
  def verifyStreams(): Map[String, StreamManager] = {

    log.info("Locating the streams {} and {}", "log-stream")
    Map(("log-stream", new StreamManager("log-stream")))

  }

  def processBatch(batch: LogBatch): Unit = {

    batchReceivedCount.incr
    batchReceivedMeter.meter {
      val data = processPayload(batch.payload, Seq[Tuple2[ByteBuffer, String]]())
      val stream = streams.get("log-stream").get.stream

      if (stream.isDefined) {
        val putData = stream.get.multiPut(data.toList)
        kinesisClient.execute(putData) onComplete {
          case Failure(f) =>
            log.error("Error trying to write records to the log-stream stream", f)
          case Success(putResult) =>
            putResult.result.getFailedRecordCount.toInt match {
              case 0 =>
                log.debug(s"Wrote ${data.size} records to log-stream")
              case count =>
                failedPutCount.incr(count.toLong)
                log.warning(s"Failed to write $count records to log-stream")
                putResult.result.getRecords.filter(r => r.getErrorCode != null && r.getErrorCode.length > 0).groupBy(_.getErrorCode).foreach { r =>
                  log.warning(s"${r._1}: ${r._2.size}")
                }
              // TODO What do we do here
            }
        }
      }
      else {
        log.error(s"Unable to utilize the stream: log-stream")
      }
    }

  }
}
