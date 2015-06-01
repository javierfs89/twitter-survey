package com.github.javifdev.survey

import twitter4j._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import scala.language.postfixOps
import scalaz.stream._
import scalaz.concurrent.Task
import scalaz.concurrent.Strategy.DefaultTimeoutScheduler

/**
 * @author Javier Fuentes Sánchez
 */
object TwitterExample1 extends App with ConfigurationTwitter {

  val twitter = new TwitterFactory(configurationBuilder.build()).getInstance()
  implicit val _dts = DefaultTimeoutScheduler

  /////////////////////////////////////////////////////

  case class RespondUser(user: User, msg: String)

  def processResponse(s: Status): RespondUser =
    if(s.getText.indexOf("#scalazMAD") > 0)
      RespondUser(s.getUser, "(╯°□°)╯︵ ┻━┻")
    else
      RespondUser(s.getUser, "(づ￣ ³￣)づ")

  // val t1 = process1 lift processResponse

  def createRequest(ru: RespondUser): StatusUpdate = ru match {
    case RespondUser(user, msg) =>
      val updateStatus: StatusUpdate = new StatusUpdate(msg)
      updateStatus.setInReplyToStatusId(user.getId)
      updateStatus
  }

  // val t2 = process1 lift createRequest

  val createResponses = process1 lift (processResponse _ andThen createRequest _)

  //////////////////////////////////////////////////////

  val polling = time.awakeEvery(30 seconds)
  val hashtag = "ScalaMAD"
  val query = new Query("#" + hashtag)
  val getQuery: Process1[Any, Query] = process1 lift { _ => query }

  def executeQuery(query: Query): Task[List[Status]] = Task { twitter.search(query).getTweets.toList }
  val queryChannel = channel lift executeQuery

  val tweets = polling pipe getQuery through queryChannel flatMap { Process emitAll _ }

  //////////////////////////////////////////////////////

  def executeRequest(su: StatusUpdate): Task[Unit] =
    Task { twitter.updateStatus(su) }
  val replySink = sink lift executeRequest

  val program = tweets pipe createResponses to replySink

  program.run.run

  // def searchProcess: Channel[Task, Any, List[Status]] = channel lift (_ => Task { twitter.search(query).getTweets.toList })

  // def reply: ((String, User)) => Task[Unit] = {
  //   case (str, user) =>
  //     val updateStatus: StatusUpdate = new StatusUpdate(str)
  //     updateStatus.setInReplyToStatusId(user.getId)
  //     Task { twitter.updateStatus(updateStatus) }
  // }

  // def replySink: Sink[Task, (String, User)] = sink lift reply

  // def fun: Status => (String, User) = status =>
  //   if(status.getText.indexOf("#scalazMAD") > 0)
  //     ("(╯°□°)╯︵ ┻━┻", status.getUser)
  //   else ("(づ￣ ³￣)づ", status.getUser)

  // def funProcess = process1 lift fun

  // time.awakeEvery(30 seconds) // Process[Task, Duration]
  //   .through(searchProcess) // Process[Task, List[Status]]
  //   .flatMap(Process.emitAll) // Process[Task, Status]
  //   .map(x => {println(x) ; x}) // .observe(io.stdOutLines) // Process[Task, Status]
  //   .pipe(funProcess) // Process[Task, (String, User)]
  //   .map(println)
  //   // .to(replySink) // Process[Task, Unit]
  //   .run.run

}
