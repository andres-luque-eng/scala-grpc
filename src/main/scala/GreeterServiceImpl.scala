package com.example.grpc

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import org.apache.pekko.actor.typed.ActorSystem

class GreeterServiceImpl(system: ActorSystem[_]) extends GreeterService {
  private implicit val ec: ExecutionContext = system.executionContext

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    Future.successful(HelloReply(s"Hello, ${in.name}! Welcome to Scala 3."))
  }
}
