package io.hydrosphere.mist.master.interfaces.http

import akka.http.scaladsl.marshalling
import akka.http.scaladsl.model
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.ParameterDirectives
import io.hydrosphere.mist.jobs.JobDetails.Source

import io.hydrosphere.mist.master.MasterService
import io.hydrosphere.mist.master.interfaces.JsonCodecs
import io.hydrosphere.mist.master.models.DevJobStartRequestModel

/**
  * Warning! - it is not part of public api!
  *
  */
object DevApi {

  import Directives._
  import JsonCodecs._
  import ParameterDirectives.ParamMagnet
  import akka.http.scaladsl.server._

  import scala.concurrent.ExecutionContext.Implicits.global

  def devRoutes(masterService: MasterService): Route = {
    path( "v2" / "hidden" / "devrun" ) {
      post( parameter('force ? false) { force =>
        entity(as[DevJobStartRequestModel]) { req =>
          val execInfo = masterService.devRun(req.toCommon, Source.Http)
          if (force)
            complete(execInfo.flatMap(_.toJobResult))
          else
            complete(execInfo.map(_.toJobStartResponse))
        }
      })
    }
  }
}
