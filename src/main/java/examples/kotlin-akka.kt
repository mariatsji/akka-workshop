package examples

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.Props


inline fun <reified T : Any> ActorRefFactory.actorOf(name: String): ActorRef {
    return actorOf(createProps<T>(), name)
}

inline fun <reified T : Any> createProps(): Props {
    return Props.create(T::class.java)
}

inline fun <reified T : Any> createProps(actor: ActorRef): Props {
    return Props.create(T::class.java, actor)
}