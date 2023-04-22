package com.kemal.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import com.kemal.model.Order.{IdAsc, Order, unmarshaller => x, _}
import com.kemal.model.Stats
import com.kemal.repo.PokemonRepo
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

import scala.util.{Failure, Success}

class PokemonController(implicit pokemonRepo: PokemonRepo) {
  val pokemonRoute: Route = {
    (path("kemal") & get) {complete("ok")} ~ (path("pokemons") & get) {
      parameters("sort".as[Order](x).?(IdAsc), "filter".as[String].?) { (sort, filter) =>
        onComplete(pokemonRepo.getPokemons(sort, filter)) {
          case Success(value) if value.isEmpty => complete(StatusCodes.NotFound, value.asJson)
          case Success(value)                  => complete(StatusCodes.OK, value.asJson)
          case Failure(exception)              => failWith(exception)
        }
      }
    } ~ (path("types") & get) {
      parameters(Symbol("sort").as[Order](x).?(NameAsc)) { sort =>
        onComplete(pokemonRepo.getTypes(sort)) {
          case Success(value) if value.isEmpty => complete(StatusCodes.NotFound, value.asJson)
          case Success(value)                  => complete(StatusCodes.OK, value.asJson)
          case Failure(exception)              => failWith(exception)
        }
      }
    } ~ (path("evolution") & get) {
      parameters(Symbol("name").as[String].?, Symbol("id").as[Int].?) {
        case (None, None) | (Some(_), Some(_)) =>
          complete(StatusCodes.BadRequest, "id or name required".asJson)
        case (maybeName, maybeId)              =>
          onComplete(pokemonRepo.getEvolution(maybeName, maybeId)) {
            case Success(value) if value.isEmpty => complete(StatusCodes.NotFound, value.asJson)
            case Success(value)                  => complete(StatusCodes.OK, value.asJson)
            case Failure(exception)              => failWith(exception)
          }
      }
    } ~ (path("detail") & get) {
      parameters(Symbol("name").as[String].?, Symbol("id").as[Int].?) {
        case (None, None) | (Some(_), Some(_)) => complete(StatusCodes.BadRequest, "id or name required".asJson)
        case (maybeName, maybeId)              =>
          onComplete(pokemonRepo.getDetail(maybeName, maybeId)) {
            case Success(value)     => complete(StatusCodes.OK, value.asJson)
            case Failure(exception) => failWith(exception)
          }
      }
    }
  }
}

object PokemonController {
  case class PokemonResponse(id: Int, name: String, frontImage: String)
  case class TypesResponse(id: Option[Int], name: String)
  case class EvolutionResponse(id: Int, name: String, evolutionChain: String)
  case class DetailResponse(
      id: Int,
      name: String,
      height: Int,
      weight: Int,
      stats: Seq[Stats],
      types: String,
      backDefault: String,
      frontDefault: String
  )
}
