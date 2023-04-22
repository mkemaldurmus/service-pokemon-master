package com.kemal.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives
import io.circe.Decoder
import io.circe.parser._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt

class PokemonClient {
  implicit val system: ActorSystem                  = ActorSystem("HttpClient")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  def get[T: Decoder](uri: String, success: StatusCode = StatusCodes.OK): Future[T] =
    Http().singleRequest(HttpRequest(uri = uri)).flatMap {
      case response if response.status == success =>
        response.entity
          .toStrict(5.seconds)
          .map(_.data.utf8String)
          .map(parse(_).flatMap(_.as[T]).fold(throw _, identity))
      case response                               => throw new RuntimeException(s"$uri parse failed. response: $response")
    }

}
