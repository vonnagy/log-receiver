package logreceiver

import com.github.vonnagy.service.container.ContainerBuilder
import logreceiver.processor.ProcessorManager
import logreceiver.routes.LogEndpoints

/**
 * Created by ivannagy on 4/8/15.
 */
object Service extends App {

  // Here we establish the container and build it while
  // applying extras.
  val service = new ContainerBuilder()
    // Add some endpoints
    .withRoutes(classOf[LogEndpoints])
    .withActors(("processor-manager", ProcessorManager.props())).build

  service.start

}
