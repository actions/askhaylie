package nutritionist

import com.amazon.speech.json.SpeechletRequestEnvelope
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet._
import com.amazon.speech.ui.{PlainTextOutputSpeech, Reprompt, SimpleCard}
import org.joda.time.{DateTime, DateTimeZone}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io.Source

/**
  * Created by sergeyzelvenskiy on 1/14/17.
  */

object Nutritionist {

  val dayToPhase = Array(1,1,2,2,3,3,3)

  def currentPhase(): String={
    val day = DateTime.now(DateTimeZone.forOffsetHours(-8)).dayOfWeek().get()
    s"P${dayToPhase(day)}"
  }

  implicit def stringToBool(s: String): Boolean=s match {
    case "1" => true
    case "0" => false
    case _  => throw new IllegalArgumentException(s"Don't know how to conver string $s to boolean")
  }

  val foods : Map[String, (String, Boolean, Boolean, Boolean)]  = Source
    .fromInputStream(this.getClass.getClassLoader.getResourceAsStream("food.csv"))
    .getLines()
    .map(_.split(",")).collect{
      case Array(food, kind, p1, p2, p3) => food -> (kind, stringToBool(p1), stringToBool(p2), stringToBool(p3))
    }.toMap

  def lookupFood(food: String ): Option[(String, Boolean, Boolean, Boolean)]= {
    val words = food.split(" ").filter(_.nonEmpty)
    if (words.length > 1) {
      foods.get(words(0))
    } else {
      foods.get(food)
    }
  }

  val phases = Set("P1", "P2", "P3")


}

class Nutritionist extends SpeechletV2{

  private val log = LoggerFactory.getLogger(classOf[Nutritionist])

  import Nutritionist._

  override def onSessionEnded(requestEnvelope: SpeechletRequestEnvelope[SessionEndedRequest]): Unit = {
    log.info(s"onSessionEnded requestId=${requestEnvelope.getRequest.getRequestId}, sessionId=${requestEnvelope.getSession.getSessionId}")
  }

  override def onIntent(requestEnvelope: SpeechletRequestEnvelope[IntentRequest]): SpeechletResponse = {
    log.info(s"User: ${requestEnvelope.getSession.getUser.getUserId}")
    val intent =requestEnvelope.getRequest.getIntent
    intent.getName match {
      case "CanIEatThatIntent" =>
        val (food, phase) = intent.getSlots.foldLeft(("", currentPhase())){
          case (params,("Food", slot)) => params.copy(_1=slot.getValue)
          case (params,("Phase", slot)) if phases.contains(slot.getValue) =>  params.copy(_2=slot.getValue)
          case (params,_) => params
        }
        //log.info(s"Got $food on phase $phase")
        lookupFood(food) match {
          case Some((kind, p1, p2,p3))=>
            if((p1 && phase=="P1") | (p2 && phase=="P2") | (p3 && phase=="P3"))
              getSpeechletResponse(s"Yes, you are allowed to eat $food during $phase.", "", false)
            else
              getSpeechletResponse(s"No, you are not allowed to eat $food during $phase.", "", false)
          case None => getSpeechletResponse("It looks like the food you are looking for is not on the list.", "", false)
        }
      case _ => getSpeechletResponse("It looks like the food you are looking for is not on the list.", "", false)
    }

  }

  override def onLaunch(requestEnvelope: SpeechletRequestEnvelope[LaunchRequest]) ={
    log.info(s"onLaunch requestId=${requestEnvelope.getRequest.getRequestId}, sessionId=${requestEnvelope.getSession.getSessionId}")
    val pt = new PlainTextOutputSpeech()
    pt.setText("Hi, this is Haylie. Welcome.")
    SpeechletResponse.newTellResponse(pt)
  }


  override def onSessionStarted(requestEnvelope: SpeechletRequestEnvelope[SessionStartedRequest]): Unit = {
    log.info(s"onSessionStarted requestId=${requestEnvelope.getRequest.getRequestId}, sessionId=${requestEnvelope.getSession.getSessionId}")
  }

  private def getSpeechletResponse(speechText: String, repromptText: String, isAskResponse: Boolean): SpeechletResponse = {
    // Create the Simple card SimpleCard.
    val card = new SimpleCard
    card.setTitle("Haylie Says")
    card.setContent(speechText)
    // Create the plain text output.
    val speech = new PlainTextOutputSpeech
    speech.setText(speechText)
    if (isAskResponse) {
      // Create reprompt
      val repromptSpeech = new PlainTextOutputSpeech
      repromptSpeech.setText(repromptText)
      val reprompt = new Reprompt
      reprompt.setOutputSpeech(repromptSpeech)
      SpeechletResponse.newAskResponse(speech, reprompt, card)
    }
    else SpeechletResponse.newTellResponse(speech, card)
  }

}
