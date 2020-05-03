/*
 * Copyright (c) 2018 https://www.reactivedesignpatterns.com/ 
 *
 * Copyright (c) 2018 https://rdp.reactiveplatform.xyz/
 *
 */

package akka.rdpextras

import akka.actor._
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.Future

object AskPattern {

  def ask(actorRef: ActorRef, timeout: Timeout, f: ActorRef ⇒ Any): Future[Any] = actorRef match {
    case ref: InternalActorRef if ref.isTerminated ⇒
      actorRef ! f(null)
      Future.failed[Any](new AskTimeoutException(s"Recipient[$actorRef] had already been terminated."))
    case ref: InternalActorRef ⇒
      if (timeout.duration.length <= 0)
        Future.failed[Any](new IllegalArgumentException(s"Timeout length must not be negative, question not sent to [$actorRef]"))
      else {
        val a = PromiseActorRef(ref.provider, timeout, targetName = actorRef.toString, "unknown")
        actorRef.tell(f(a), a)
        a.result.future
      }
    case _ ⇒ Future.failed[Any](new IllegalArgumentException(s"Unsupported recipient ActorRef type, question not sent to [$actorRef]"))
  }

}
