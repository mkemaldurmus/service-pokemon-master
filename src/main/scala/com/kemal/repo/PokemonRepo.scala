package com.kemal.repo

import akka.actor.ActorSystem
import com.kemal.controller.PokemonController.{DetailResponse, EvolutionResponse, PokemonResponse, TypesResponse}
import com.kemal.model.Order.{IdAsc, IdDesc, NameAsc, NameDesc, Order}
import com.kemal.model._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

class PokemonRepo()(implicit
    val ctx: PostgresAsyncContext[LowerCase.type],
    val system: ActorSystem,
    val ec: ExecutionContext
) {

  import ctx._

  implicit val encodeStatsType: MappedEncoding[String, Seq[Stats]] =
    MappedEncoding[String, Seq[Stats]](decode[Seq[Stats]](_).fold(throw _, r => r))
  implicit val decodeStatsType: MappedEncoding[Seq[Stats], String] =
    MappedEncoding[Seq[Stats], String](_.asJson.noSpaces)

  private val pokemonQ = quote(querySchema[PokemonDto]("pokemon"))
  private val typeQ   = quote(querySchema[TypeDto]("types"))

  def insertPokemon(pokemonDto: PokemonDto): Future[Long] = {
    ctx.run(pokemonQ.insert(lift(pokemonDto)))
  }

  def insertType(typeDto: TypeDto): Future[Long] = {
    ctx.run(typeQ.insert(lift(typeDto)))
  }

  def getTypes(sort: Order): Future[List[TypesResponse]] = {
    val query =
      sort match {
        case NameAsc  => typeQ.dynamic.sortBy(_.name)(Ord.asc)
        case NameDesc => typeQ.dynamic.sortBy(_.name)(Ord.desc)
      }
    ctx.run(query.map(result => TypesResponse(Some(result.pid), result.name)))
  }

  def getPokemons(sort: Order, filter: Option[String]): Future[List[PokemonResponse]] = {
    val query = sort match {
      case IdAsc    =>
        join(filter).sortBy(_._1.id)(Ord.asc)
      case IdDesc   =>
        join(filter).sortBy(_._1.id)(Ord.desc)
      case NameAsc  =>
        join(filter).sortBy(_._1.name)(Ord.asc)
      case NameDesc =>
        join(filter).sortBy(_._1.name)(Ord.desc)
    }
    ctx.run(query.map(tuple => PokemonResponse(tuple._1.id, tuple._1.name, tuple._1.frontDefault)).distinct)
  }

  private def join(filter: Option[String]): ctx.DynamicQuery[(PokemonDto, Option[TypeDto])] = {
    pokemonQ.dynamic
      .leftJoin(typeQ)
      .on(_.id == _.pid)
      .filterOpt(filter)((x, y) => quote(x._2.map(_.name).getOrNull).equals(y))
  }

  def getEvolution(name: Option[String], id: Option[Int]): Future[List[EvolutionResponse]] = {
    val query = pokemonQ.dynamic
      .filterOpt(name)((pokemonDetail, name) => quote(pokemonDetail.name).equals(name))
      .filterOpt(id)((pokemonDetail, id) => quote(pokemonDetail.id).equals(id))
    ctx.run(query.map(result => EvolutionResponse(result.id, result.name, result.evolution)))
  }

  def getDetail(name: Option[String], id: Option[Int]): Future[DetailResponse] = {
    val query = pokemonQ.dynamic
      .join(typeQ)
      .on(_.id == _.pid)
      .filterOpt(name)((pokemonDetail, name) => quote(pokemonDetail._1.name).equals(name))
      .filterOpt(id)((pokemonDetail, id) => quote(pokemonDetail._1.id).equals(id))
    ctx
      .run(query)
      .map { res =>
        val types = res.map(_._2.name)
        (res.map(_._1).head, types)
      }
      .map { case (pokemon, types) =>
        DetailResponse(
          pokemon.id,
          pokemon.name,
          pokemon.height,
          pokemon.weight,
          pokemon.stats,
          types.toString(),
          pokemon.backDefault,
          pokemon.backDefault
        )
      }
  }

  def deleteType(name: String): Future[Long] = ctx.run(typeQ.filter(_.name == lift(name)).delete)
  def deletePokemon(id: Int): Future[Long]    = ctx.run(pokemonQ.filter(_.id == lift(id)).delete)
}
