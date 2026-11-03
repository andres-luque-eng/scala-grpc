package com.example.grpc

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import scala.compiletime.uninitialized
import scala.concurrent.ExecutionContext

class GreeterServiceImplSpec extends munit.FunSuite {

  private var system: ActorSystem[Nothing] = uninitialized
  private var service: GreeterServiceImpl = uninitialized
  private implicit var ec: ExecutionContext = uninitialized

  override def beforeAll(): Unit = {
    system = ActorSystem[Nothing](Behaviors.empty[Nothing], "GreeterServiceImplSpecSystem")
    service = new GreeterServiceImpl(system)
    ec = system.executionContext
  }

  override def afterAll(): Unit = {
    system.terminate()
  }

  test("sayHello returns greeting with provided name") {
    service.sayHello(HelloRequest("Alice")).map { reply =>
      assertEquals(reply.message, "Hello, Alice! Welcome to Scala 3.")
    }
  }

  test("sayHello handles different names correctly") {
    service.sayHello(HelloRequest("Bob")).map { reply =>
      assertEquals(reply.message, "Hello, Bob! Welcome to Scala 3.")
    }
  }
}
