package examples

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


object ComputeFuture extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5 seconds)

  class ComputeActor extends Actor {
    def receive = { case s : String => sender ! s.length }
  }

  val system = ActorSystem()
  val computeActor = system.actorOf(Props(new ComputeActor()))

  val f1 = (computeActor ? "abc").mapTo[Int]
  val f2 = (computeActor ? "bcd").mapTo[Int]

  val futures: Future[List[Int]] = Future.sequence(List(f1, f2))

  futures.map(numbers => numbers.sum)
    .recoverWith {
      case e: NullPointerException => Future { 0 }
    }
    .onComplete(result => println(result))

  Thread.sleep(1000)
  system.terminate()
}
