package com.example.grpc

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import scala.util.{Success, Failure}
import scala.concurrent.duration.*
import scala.concurrent.Await

object Server {
  def main(args: Array[String]): Unit = {
    // Launch the ActorSystem and bind the gRPC service
    val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
      val system = context.system
      implicit val sys: ActorSystem[_] = system
      implicit val ec = system.executionContext
      system.log.info("Starting server!")

      val service = new GreeterServiceImpl(system)
      val handler = GreeterServiceHandler.withServerReflection(service)

      // Customize the host and port as needed
      val bindingFuture = Http().newServerAt("0.0.0.0", 3030).bind(handler)

      bindingFuture.onComplete {
        case Success(bound) =>
          system.log.info(s"gRPC Server alive & listening on: http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}")
        case Failure(ex) =>
          system.log.error("Critical error binding gRPC port!", ex)
          system.terminate()
      }

      // Hook turn off signal to gracefully shutdown the server
      Runtime.getRuntime.addShutdownHook(new Thread() {
        override def run(): Unit = {
          system.log.info("Shutdown signal received. Shutting down sockets...")
          val unbound = bindingFuture.flatMap(_.unbind())
          unbound.foreach(_ => system.terminate())
          Await.result(system.whenTerminated, 15.seconds)
        }
      })

      // Keep the ActorSystem alive until it is terminated
      Behaviors.empty
    }, "gRPCServerSystem")

    // Block the main thread until the ActorSystem is terminated
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
