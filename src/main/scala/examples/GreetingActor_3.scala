package examples

import akka.actor._

import scala.concurrent.duration._
import scala.language.postfixOps

case class DoGreeting()

class GreetingActor_3(myInterval: FiniteDuration) extends Actor {

  override def preStart() = {
    scheduleNextGreeting()
  }

  def receive = {
    case DoGreeting => {
      println("Hello!")
      scheduleNextGreeting()
    }
  }

  def scheduleNextGreeting() {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(myInterval, self, DoGreeting)
  }

}

object GreetingActor_3 extends App {
  val system = ActorSystem("MySystem")
  val actorRef = system.actorOf(Props(new GreetingActor_3(1 second)))
}
