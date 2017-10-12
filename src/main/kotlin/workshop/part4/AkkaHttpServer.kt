package workshop.part4

import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.stream.ActorMaterializer
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserService
import workshop.part2.FraudWordActor
import workshop.part2.UserActor
import workshop.part2.supervisor.VettingActorFactory
import workshop.part2.supervisor.VettingSupervisor

class AkkaHttpServer {

    private fun start(system: ActorSystem) {
        val userActor = system.actorOf(Props.create(UserActor::class.java) { UserActor(UserService()) })
        val fraudWordActor = system.actorOf(Props.create(FraudWordActor::class.java) { FraudWordActor(FraudWordService()) })

        val vettingSupervisor = system.actorOf(Props.create(VettingSupervisor::class.java) { VettingSupervisor(VettingActorFactory(userActor, fraudWordActor)) })

        val http = Http.get(system)

        val materializer = ActorMaterializer.create(system)

        val flow = HttpRoutes(vettingSupervisor, VerdictCache()).registerRoutes().flow(system, materializer)

        http.bindAndHandle(flow, ConnectHttp.toHost(
                HOST_BINDING, PORT), materializer)

        println(String.format("Server online at http://%s:%d/", HOST_BINDING, PORT))

        sleepForSeconds(24 * 60 * 60)
    }

    companion object {

        val HOST_BINDING = "localhost"
        val PORT = 8080

        @JvmStatic
        fun main(args: Array<String>) {
            val app = AkkaHttpServer()

            app.start(ActorSystem.create("routes"))
        }

        private fun sleepForSeconds(seconds: Int) {
            try {
                Thread.sleep((seconds * 1000).toLong())
            } catch (i: Exception) {
            }

        }
    }

}
