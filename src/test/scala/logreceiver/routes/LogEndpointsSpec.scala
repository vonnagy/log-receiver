package logreceiver.routes

import org.specs2.mutable.Specification
import spray.http.HttpHeaders.{Accept, RawHeader, `Content-Length`}
import spray.http.{MediaTypes, StatusCodes}
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

class LogEndpointsSpec extends Specification with Specs2RouteTest with HttpService {

  implicit def actorRefFactory = system

  val endpoints = new LogEndpoints
  val logSingleLine = "<13>1 2013-09-25T01:16:49.402923+00:00 host token web.1 - [meta sequenceId=\"2\"] other message\n"

  "The log routing infrastructure" should {

    "return MethodNotAllowed error when not a POST" in {
      Get("/logs") ~> sealRoute(endpoints.route) ~> check {
        status must be(StatusCodes.MethodNotAllowed)
      }
    }

    "return NotAcceptable error when `Accept` header has the wrong type" in {
      Post("/logs").withHeaders(Accept(MediaTypes.`text/plain`)) ~> sealRoute(endpoints.route) ~> check {
        status must be(StatusCodes.NotAcceptable)
      }
    }

    "return NotFound when the correct method and media type are defined, but other headers are missing" in {
      Post("/logs").withHeaders(Accept(logreceiver.`application/logplex-1`)) ~> sealRoute(endpoints.route) ~> check {

        status must be(StatusCodes.NotFound)
      }
    }

    "return NoContent when  the correct method and media type are defined" in {
      Post("/logs")
        .withHeaders(Accept(logreceiver.`application/logplex-1`),
          `Content-Length`(730),
          RawHeader(logreceiver.`Logplex-Msg-Count`, 10.toString),
          RawHeader(logreceiver.`Logplex-Drain-Token`, "d.fc6b856b-3332-4546-93de-7d0ee272c3bd"),
          RawHeader(logreceiver.`Logplex-Frame-Id`, "09C557EAFCFB6CF2740EE62F62971098"))
        .withEntity(logSingleLine) ~> sealRoute(endpoints.route) ~> check {

        status must be(StatusCodes.NoContent)
      }
    }
  }

  step {
    if (!system.isTerminated) {
      system.shutdown
      system.awaitTermination
    }
  }
}