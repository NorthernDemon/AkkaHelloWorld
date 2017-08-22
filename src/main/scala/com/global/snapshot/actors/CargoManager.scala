package com.global.snapshot.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.global.snapshot.Config
import com.global.snapshot.actors.CargoManager.{Start, Stop}
import com.global.snapshot.actors.CargoScheduler.StartScheduling
import com.global.snapshot.actors.CargoStation.Connect

class CargoManager
  extends Actor with ActorLogging {

  override def receive = {

    case Start =>
      val stockholm = context.actorOf(CargoStation.props(Config.cargoStationsCount), "stockholm")
      val copenhagen = context.actorOf(CargoStation.props(Config.cargoStationsCount), "copenhagen")
      val helsinki = context.actorOf(CargoStation.props(Config.cargoStationsCount), "helsinki")
      val oslo = context.actorOf(CargoStation.props(Config.cargoStationsCount), "oslo")

      stockholm ! Connect(
        incomingChannels = Set(copenhagen, helsinki),
        outgoingChannels = Set(oslo)
      )
      copenhagen ! Connect(
        incomingChannels = Set(oslo),
        outgoingChannels = Set(stockholm, helsinki)
      )
      helsinki ! Connect(
        incomingChannels = Set(copenhagen),
        outgoingChannels = Set(stockholm, oslo)
      )
      oslo ! Connect(
        incomingChannels = Set(stockholm, helsinki),
        outgoingChannels = Set(copenhagen)
      )

      stockholm ! StartScheduling
      copenhagen ! StartScheduling
      helsinki ! StartScheduling
      oslo ! StartScheduling

    case Stop =>
      context stop self

    case event =>
      log.error(s"Cargo manager received an unknown event $event")
  }
}

object CargoManager {
  def props: Props =
    Props(new CargoManager)

  sealed trait CargoManagerOperations
  case object Start extends CargoManagerOperations
  case object Stop extends CargoManagerOperations
}