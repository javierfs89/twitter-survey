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
object TwitterExample3 extends App with ConfigurationTwitter {

  val twitter = new TwitterFactory(configurationBuilder.build()).getInstance()
  implicit val _dts = DefaultTimeoutScheduler

  /////////////////////////////////////////////////////

  case class RespondUser(user: User, msg: String)

  def processResponse(s: Status): RespondUser =
    if(s.getText.indexOf("#scalazMAD") > 0)
      RespondUser(s.getUser, "(╯°□°)╯︵ ┻━┻")
    else
      RespondUser(s.getUser, "(づ￣ ³￣)づ")

  def createRequest(ru: RespondUser): StatusUpdate = ru match {
    case RespondUser(user, msg) =>
      val updateStatus: StatusUpdate = new StatusUpdate(msg)
      updateStatus.setInReplyToStatusId(user.getId)
      updateStatus
  }

  val createResponses = process1 lift (processResponse _ andThen createRequest _)

  //////////////////////////////////////////////////////

  val hashtag = "ScalaMAD"
  val query = new Query("#" + hashtag)

  def executeQuery(query: Query): Task[List[Status]] = Task { twitter.search(query).getTweets.toList }
  val queryChannel = channel lift executeQuery

  //////////////////////////////////////////////////////

  def executeRequest(su: StatusUpdate): Task[Unit] =
    Task { twitter.updateStatus(su) }
  val replySink = sink lift executeRequest

  //////////////////////////////////////////////////////

  def getUser(s: Status): String = s.getUser.getName
  val t6 = process1 lift getUser

  val t7 = io.fileChunkW("blacklist.txt") pipeIn text.utf8Encode

  val writeToFile = t7 pipeIn t6

  val theOtherSink = replySink pipeIn createResponses

  //////////////////////////////////////////////////////

  def getQuery2(hashtag: String): Query =
    new Query(hashtag)
  def getQueries2(hashtag: String): Process1[Any, Query] = process1 lift {_ => getQuery2(hashtag)}

  val scalaMadQueries = time.awakeEvery(30 seconds) pipe getQueries2("#ScalaMad")

  val scalaMadStreamsQueries = time.awakeEvery(90 seconds) pipe getQueries2("#ScalaMadStreams")

  val queries = scalaMadQueries.wye(scalaMadStreamsQueries)(wye.merge)

  val tweets = queries through queryChannel flatMap { Process emitAll _ }

  val program = tweets observe writeToFile to theOtherSink

  program.run.run

}
