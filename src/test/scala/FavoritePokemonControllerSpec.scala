import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kemal.Boot.postgresCtxLower
import com.kemal.controller.FavoritePokemonController
import com.kemal.controller.FavoritePokemonController.FavoritePokemonList
import com.kemal.repo.FavoritePokemonRepo
import de.heikoseeberger.akkahttpcirce._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class FavoritePokemonControllerSpec()
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with FailFastCirceSupport {
  lazy val routes: Route                                = new FavoritePokemonController().favoriteRout
  implicit val favoritePokemonRepo: FavoritePokemonRepo = new FavoritePokemonRepo()
  val httpEntity: (String) => HttpEntity.Strict         = (str: String) => HttpEntity(ContentTypes.`application/json`, str)
  val inputPokemon: FavoritePokemonList                 = FavoritePokemonList(
    "listname",
    Seq("pokemonNames")
  )

  override def beforeAll(): Unit = {
    Await.result(favoritePokemonRepo.insertFavoriteList(inputPokemon), 2 seconds)
    ()
  }

  override def afterAll(): Unit = {
    Await.result(favoritePokemonRepo.deleteList(inputPokemon.list_name), 2 seconds)
    super.afterAll()
    ()
  }
  "FavoritePokemonController" should {
    "return Created  valid user" in {
      val validUser =
        """
          {
            "list_name": "kemal",
            "pokemon_name": ["pikachu","balbazar"]
          }
        """
      Post("/favorites", httpEntity(validUser)) ~> routes ~> check {
        status should ===(StatusCodes.Created)
        responseAs[Long] shouldBe 1L

      }
    }
    "return Accept valid name" in {

      Delete("/favorites?name=kemal") ~> routes ~> check {
        status should ===(StatusCodes.Accepted)
        responseAs[Long] shouldBe 1L
      }
    }
  }
}
