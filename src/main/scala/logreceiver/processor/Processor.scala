package logreceiver.processor

import akka.actor.{Actor, ActorLogging, Stash}
import com.github.vonnagy.service.container.health.HealthInfo

/**
 * Derive any processors by applying this trait and then add it to log processors
 * in the configuraiton file
 */
trait Processor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"${this.getClass.getName} starting at ${context.self.path}")
  }

  override def postStop(): Unit = {
    log.info(s"${this.getClass.getName} is stopping")
  }

  override def receive = health orElse {
    case ProcessorReady => // We are ready to go
      context.become(running orElse health)
      log.info(s"${this.getClass.getName} is ready to receive messages")
      context.parent ! ProcessorReady
    case _ =>
  }

  def health: Actor.Receive = {
    case CheckHealth => // How are we doing
      sender ! getHealth
  }

  /** Must be implemented by an Actor. */
  def running: Receive

  /** Must be implemented by an Actor. */
  def getHealth: HealthInfo
}

