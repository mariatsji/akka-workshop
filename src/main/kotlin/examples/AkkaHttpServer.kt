package examples

import akka.Done
import akka.actor.ActorSystem
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.AllDirectives
import akka.http.javadsl.server.PathMatchers.longSegment
import akka.http.javadsl.server.Route
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javaslang.collection.List
import javaslang.control.Option
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


class AkkaHttpServer : AllDirectives() {

    private fun createRoute(): Route {

        return route(
                get {
                    pathPrefix("item") {
                        path(longSegment()) { id: Long ->
                            val futureMaybeItem = fetchItem(id)
                            val routeAdapter = onSuccess({ futureMaybeItem }
                            ) { maybeItem ->
                                maybeItem.map({ item -> completeOK(item, Jackson.marshaller()) })
                                        .getOrElse({ complete(StatusCodes.NOT_FOUND, "Not Found") })
                            }
                            routeAdapter
                        }
                    }
                },
                post {
                    path("create-order") {
                        entity(Jackson.unmarshaller(Order::class.java)) { order ->
                            val futureSaved = saveOrder(order)
                            onSuccess({ futureSaved }
                            ) { _ -> complete("order created") }
                        }
                    }
                }
        )
    }

    // (fake) async database query api
    private fun fetchItem(itemId: Long): CompletionStage<Option<Item>> {
        return CompletableFuture.completedFuture(Option.of(Item("foo", itemId)))
    }

    // (fake) async database query api
    private fun saveOrder(order: Order): CompletionStage<Done> {
        println(order)
        return CompletableFuture.completedFuture(Done.getInstance())
    }

    private class Item @JsonCreator
    internal constructor(@param:JsonProperty("name") val name: String,
                         @param:JsonProperty("id") val id: Long)

    private class Order @JsonCreator
    internal constructor(@param:JsonProperty("items") val items: List<Item>)

    companion object {

        val HOST_BINDING = "localhost"
        val PORT = 8080

        @JvmStatic public fun main(args: Array<String>) {
            // boot up server using the route as defined below
            val system = ActorSystem.create("routes")

            val http = Http.get(system)
            val materializer = ActorMaterializer.create(system)

            val app = AkkaHttpServer()

            val flow = app.createRoute().flow(system, materializer)

            http.bindAndHandle(flow, ConnectHttp.toHost(
                    HOST_BINDING, PORT), materializer)

            System.out.println(String.format("Server online at http://%s:%d/", HOST_BINDING, PORT))

            sleepForSeconds(30)
        }

        private fun sleepForSeconds(seconds: Int) {
            try {
                Thread.sleep(seconds * 1000L)
            } catch (i: Exception) {
            }

        }
    }
}
