package com.example.grpc

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.grpc.GrpcClientSettings
import com.typesafe.config.ConfigFactory
import scala.compiletime.uninitialized
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.*

class GreeterServiceIntegrationSpec extends munit.FunSuite {

  private val config = ConfigFactory.parseString("pekko.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())
    .resolve()

  private var system: ActorSystem[Nothing] = uninitialized
  private var binding: Http.ServerBinding = uninitialized
  private var client: GreeterServiceClient = uninitialized
  private implicit var ec: ExecutionContext = uninitialized

  override def beforeAll(): Unit = {
    system = ActorSystem[Nothing](Behaviors.empty[Nothing], "GreeterIntegrationSpecSystem", config)
    implicit val sys: ActorSystem[?] = system
    ec = system.executionContext

    val service = new GreeterServiceImpl(system)
    val handler = GreeterServiceHandler.withServerReflection(service)

    // Bind to port 0 (ephemeral port allocated by OS) to avoid any port conflicts
    val bindingFuture = Http().newServerAt("127.0.0.1", 0).bind(handler)
    binding = Await.result(bindingFuture, 10.seconds)

    val boundPort = binding.localAddress.getPort

    val clientSettings = GrpcClientSettings
      .connectToServiceAt("127.0.0.1", boundPort)
      .withTls(false)

    client = GreeterServiceClient(clientSettings)
  }

  override def afterAll(): Unit = {
    if (client != null) client.close()
    if (binding != null) Await.result(binding.unbind(), 10.seconds)
    if (system != null) {
      system.terminate()
      Await.result(system.whenTerminated, 10.seconds)
    }
  }

  test("gRPC client invokes SayHello over HTTP/2 loopback") {
    client.sayHello(HelloRequest("IntegrationUser")).map { reply =>
      assertEquals(reply.message, "Hello, IntegrationUser! Welcome to Scala 3.")
    }
  }
}
