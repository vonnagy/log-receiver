import spray.http.HttpHeaders.RawHeader
import spray.http.{MediaType, MediaTypes}
import spray.routing.directives.HeaderDirectives.headerValuePF

/**
 * Created by ivannagy on 4/9/15.
 */
package object logreceiver {

  val `application/logplex-1` = MediaTypes.register(MediaType.custom("application", "logplex-1", true, false, Nil, Map.empty))

  val `Logplex-Msg-Count` = "Logplex-Msg-Count"
  val `Logplex-Frame-Id` = "Logplex-Frame-Id"
  val `Logplex-Drain-Token` = "Logplex-Drain-Token"

  val logplexMsgCount =
    headerValuePF {
      case RawHeader(`Logplex-Msg-Count`, count) => count.toInt
    }

  val logplexFrameId =
    headerValuePF {
      case RawHeader(`Logplex-Frame-Id`, id) => id
    }

  val logplexToken =
    headerValuePF {
      case RawHeader(`Logplex-Drain-Token`, token) => token
    }
}
