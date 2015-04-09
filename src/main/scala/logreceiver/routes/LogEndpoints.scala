package logreceiver.routes

import akka.actor.{ActorSystem, ActorRefFactory}
import com.github.vonnagy.service.container.http.routing.RoutedEndpoints
import spray.http.{MediaType, MediaTypes, StatusCodes}

/**
 * Created by ivannagy on 4/9/15.
 */
class LogEndpoints(implicit system: ActorSystem,
                   actorRefFactory: ActorRefFactory) extends RoutedEndpoints {

    val `application/logplex-1` = MediaTypes.register(MediaType.custom("application", "logplex-1", true, false, Nil, Map.empty))

    // Import the default Json marshaller and un-marshaller
    implicit val marshaller = jsonMarshaller
    implicit val unmarshaller = jsonUnmarshaller[Product]

    val route = {
      post {
        path("logs") {
          acceptableMediaTypes(`application/logplex-1`) {
            respondWithHeader(spray.http.HttpHeaders.`Content-Length`(0)) {
              complete(StatusCodes.NoContent)
            }
          }
        }
      }
    }
  }