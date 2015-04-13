package logreceiver.processor

import java.nio.ByteBuffer

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActorRef
import com.github.vonnagy.service.container.metrics.Counter
import com.typesafe.config.ConfigFactory
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

/**
 * Created by ivannagy on 4/13/15.
 */
class ProcessorSpec extends Specification with Mockito {

  implicit val system = ActorSystem("test", ConfigFactory.parseString(
    """
      log {
        processors {
          test {
            # The name of the processor class
            class = "logreceiver.processor.TestProcessor"
            # Is the processor enabled
            enabled = on
          }
        }
      }
    """.stripMargin))

  val payload = """281 <158>1 2015-04-13T17:27:38.618144+00:00 host t.58895e6d-a50a-496d-9ad6-96f4667bb1d7 router - at=info method=POST path="/v1/search" host=wsback-3.presage.io request_id=2a5bd782-529d-4aef-8817-22b3bcc69b75 fwd="82.12.180.135" dyno=web.40 connect=0ms service=2ms status=200 bytes=371
281 <158>1 2015-04-13T17:27:38.618144+00:00 host t.58895e6d-a50a-496d-9ad6-96f4667bb1d7 router - at=info method=POST path="/v1/search" host=wsback-3.presage.io request_id=2a5bd782-529d-4aef-8817-22b3bcc69b75 fwd="82.12.180.135" dyno=web.41 connect=0ms service=2ms status=200 bytes=371
281 <158>1 2015-04-13T17:27:38.618144+00:00 host t.58895e6d-a50a-496d-9ad6-96f4667bb1d7 router - at=info method=POST path="/v1/search" host=wsback-3.presage.io request_id=2a5bd782-529d-4aef-8817-22b3bcc69b75 fwd="82.12.180.135" dyno=web.42 connect=0ms service=2ms status=200 bytes=371
281 <158>1 2015-04-13T17:27:38.618144+00:00 host t.58895e6d-a50a-496d-9ad6-96f4667bb1d7 router - at=info method=POST path="/v1/search" host=wsback-3.presage.io request_id=2a5bd782-529d-4aef-8817-22b3bcc69b75 fwd="82.12.180.135" dyno=web.43 connect=0ms service=2ms status=200 bytes=371
281 <158>1 2015-04-13T17:27:38.618144+00:00 host t.58895e6d-a50a-496d-9ad6-96f4667bb1d7 router - at=info method=POST path="/v1/search" host=wsback-3.presage.io request_id=2a5bd782-529d-4aef-8817-22b3bcc69b75 fwd="82.12.180.135" dyno=web.44 connect=0ms service=2ms status=200 bytes=371
70 <174>1 2012-07-22T00:06:26+00:00 host erlang console - Hi from erlang""" + "\n"

  "The processor" should {

    "parse a log batch correctly" in {

      val mgr = TestActorRef[TestProcessor](Props(Class.forName("logreceiver.processor.TestProcessor")))

      val p = mgr.underlyingActor.processPayload(payload, Seq[Tuple2[ByteBuffer, String]]())
      p.size must be equalTo (6)
      p(0)._2 must beEqualTo("t.58895e6d-a50a-496d-9ad6-96f4667bb1d7")
      p(0)._1.array().last must beEqualTo('\n')

      p(p.size - 1)._2 must beEqualTo("unknown")
      p(p.size - 1)._1.array().last must beEqualTo('\n')
    }
  }

  step {
    if (!system.isTerminated) {
      system.shutdown
      system.awaitTermination
    }
  }

}
