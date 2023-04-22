package com.kemal

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.kemal.client.PokemonClient
import com.kemal.config.AppConfig.postgresConfig
import com.kemal.controller.{FavoritePokemonController, PokemonController}
import com.kemal.repo.{FavoritePokemonRepo, PokemonRepo}
import com.typesafe.scalalogging.StrictLogging
import io.getquill.{LowerCase, PostgresAsyncContext}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

trait Complements extends StrictLogging {

  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  lazy val routes: Route = Route.seal { pokemonController.pokemonRoute ~ favoritePokemonController.favoriteRout }

  /** Data stores */
  implicit lazy val postgresCtxLower: PostgresAsyncContext[LowerCase.type] =
    new PostgresAsyncContext(LowerCase, postgresConfig)

  /** Repos */
  implicit lazy val pokemonRepo: PokemonRepo                 = new PokemonRepo
  implicit lazy val favoritePokemonRepo: FavoritePokemonRepo = new FavoritePokemonRepo

  /** Client */
  implicit lazy val pokemonClient: PokemonClient = new PokemonClient()

  /** Controller */
  implicit lazy val pokemonController: PokemonController                 = new PokemonController
  implicit lazy val favoritePokemonController: FavoritePokemonController = new FavoritePokemonController

  def withHandlingErrorF(body: => Future[_]): Any = {
    try {
      body.recover { case t: Throwable =>
        logger.error("Uncaught exception occurred.", t)
        releaseResources(1)
      }
    } catch {
      case t: Throwable =>
        logger.error("Uncaught exception occurred.", t)
        releaseResources(1)
    }
  }

  def releaseResources(code: Int): Unit = {
    Await.result(system.terminate(), 15.seconds)
    System.exit(code)
  }
}
