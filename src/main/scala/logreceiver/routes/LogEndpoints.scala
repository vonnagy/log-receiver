package logreceiver.routes

import akka.actor.{ActorRefFactory, ActorSystem}
import com.github.vonnagy.service.container.http.routing.RoutedEndpoints
import com.github.vonnagy.service.container.log.LoggingAdapter
import com.github.vonnagy.service.container.metrics.{Meter, Counter}
import logreceiver.processor.LogBatch
import logreceiver.{logplexFrameId, logplexMsgCount, logplexToken}
import spray.http.{HttpHeaders, StatusCodes}

import scala.util.Try

/**
 * Created by ivannagy on 4/9/15.
 */
class LogEndpoints(implicit system: ActorSystem,
                   actorRefFactory: ActorRefFactory) extends RoutedEndpoints with LoggingAdapter {

  val logCount = Counter("http.log.receive")
  val logMeter = Meter("http.log.receive.meter")
  val logFailedCount = Counter("http.log.receive.failed")

  val route = {
    post {
      path("logs") {
        logRequest("log-received", akka.event.Logging.DebugLevel) {
          acceptableMediaTypes(logreceiver.`application/logplex-1`) {
            requestEntityPresent {
              logplexMsgCount { msgCount =>
                logplexToken { token =>
                  logplexFrameId { frameId =>
                    entity(as[String]) { payload =>
                      respondWithHeader(HttpHeaders.`Content-Length`(0)) { ctx =>
                        Try({
                          // Publish the batch to the waiting processor(s)
                          system.eventStream.publish(LogBatch(token, frameId, msgCount, payload))
                          // Increment the counter
                          logCount.incr
                          logMeter.mark
                          // Mark the request as complete
                          ctx.complete(StatusCodes.NoContent)
                        }) recover {
                          case e =>
                            log.error(s"Unable to handle the log: $logplexFrameId", e)
                            ctx.complete(StatusCodes.InternalServerError)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}