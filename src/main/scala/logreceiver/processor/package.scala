package logreceiver

/**
 * Created by ivannagy on 4/10/15.
 */
package object processor {

  /**
   * This is the message that is sent to the processors
   * @param drainToken the app's token
   * @param frameId the frame ide
   * @param count the number of log rows in the payload
   * @param payload this is the raw payload
   */
  case class LogBatch(drainToken: String, frameId: String, count: Int, payload: String)

  /**
   * This is the message that the processor manager will use to query the health
   * of each processor
   */
  case object CheckHealth

  /**
   * This is the message the a processor sends to itself when it is ready to accept
   * messages
   */
  case object ProcessorReady
}
