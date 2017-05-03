package io.hydrosphere.mist.master.interfaces.http

import io.hydrosphere.mist.api._
import io.hydrosphere.mist.jobs._
import io.hydrosphere.mist.jobs.jar._

case class HttpJobInfo(
  name: String,
  execute: Option[Map[String, HttpJobArg]] = None,
  train:   Option[Map[String, HttpJobArg]] = None,
  serve:   Option[Map[String, HttpJobArg]] = None,

  isHiveJob: Boolean = false,
  isSqlJob: Boolean = false,
  isStreamingJob: Boolean = false,
  isMLJob: Boolean = false,
  isPython: Boolean = false
)

object HttpJobInfo {

  def forPython(name: String) = HttpJobInfo(name = name, isPython = true)

  def convert(info: JobInfo): HttpJobInfo = info match {
    case py: PyJobInfo => HttpJobInfo.forPython(info.definition.name)
    case jvm: JvmJobInfo =>
      val inst = jvm.jobClass
      val classes = inst.supportedClasses()
      HttpJobInfo(
        name = info.definition.name,
        execute = inst.execute.map(i => i.argumentsTypes.mapValues(HttpJobArg.convert)),
        train = inst.train.map(i => i.argumentsTypes.mapValues(HttpJobArg.convert)),
        serve = inst.serve.map(i => i.argumentsTypes.mapValues(HttpJobArg.convert)),

        isHiveJob = classes.contains(classOf[HiveSupport]),
        isSqlJob = classes.contains(classOf[SQLSupport]),
        isStreamingJob = classes.contains(classOf[StreamingSupport]),
        isMLJob = classes.contains(classOf[MLMistJob])
      )
  }
}

case class HttpJobArg(
  `type`: String,
  args: Seq[HttpJobArg]
)

object HttpJobArg {

  def convert(argType: JobArgType): HttpJobArg = {
    val t = argType.getClass.getSimpleName.replace("$", "")
    val typeArgs = argType match {
      case x @ (MInt | MDouble| MString | MAny) => Seq.empty
      case x: MMap => Seq(x.k, x.v).map(HttpJobArg.convert)
      case x: MList => Seq(HttpJobArg.convert(x.v))
      case x: MOption => Seq(HttpJobArg.convert(x.v))
    }
    new HttpJobArg(t, typeArgs)
  }
}


