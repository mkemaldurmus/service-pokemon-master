package com.kemal

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.kemal.config.AppConfig.{limit, url}
import com.kemal.model._
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Importer extends App with Complements {

  Source
    .future(pokemonData)
    .mapConcat(identity)
    .via(processingFlow)
    .runWith(Sink.fold(0L) { case (acc, stats) =>
      val total = acc + stats
      if (total % 100 == 0) {
        logger.info(s"Processed $total pokemon.")
      }
      total
    })
    .onComplete {
      case Success(total) =>
        logger.info(s"Completed, total pokemon count: $total")
        releaseResources(0)
      case Failure(ex)    =>
        logger.error("Failed:", ex)
        releaseResources(1)
    }

  private def pokemonData: Future[Seq[PokemonInfo]] = {
    pokemonClient
      .get[ClientResponse](s"$url?limit=$limit")
      .map(_.results)
  }

  private def processingFlow: Flow[PokemonInfo, Long, NotUsed] = Flow[PokemonInfo].mapAsync(1) { pokemonInfo =>
    for {
      pokemonDetail    <- pokemonClient.get[PokemonDetail](pokemonInfo.url)
      evolutionChain   <- pokemonClient.get[EvolutionChain](pokemonDetail.species.map(_.url).get)
      evolutionPokemon <- pokemonClient.get[EvolutionPokemon](evolutionChain.evolution_chain.url)
      evolution         = evolutionPokemon.chain.evolves_to.map(_.species.name) ++ evolutionPokemon.chain.evolves_to.flatMap(
                            _.evolves_to.map(_.species.name)
                          )
      count            <- {
        val pokemonDto = pokemonDetail.preparePokemonDto(evolution)
        pokemonRepo.insertPokemon(pokemonDto)
      }
      _                 = pokemonDetail.types.map(res => pokemonRepo.insertType(TypeDto(pokemonDetail.id, res.`type`.name)))
    } yield count
  }
}
