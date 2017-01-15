package nutritionist

import com.amazon.speech.json.SpeechletRequestEnvelope
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet._
import com.amazon.speech.ui.{PlainTextOutputSpeech, Reprompt, SimpleCard}
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by sergeyzelvenskiy on 1/14/17.
  */

object Nutritionist {

  val dayToPhase = Array(1,1,2,2,3,3,3)

  def currentPhase()={
    val day = DateTime.now(DateTimeZone.forOffsetHours(-8)).dayOfWeek().get()
    s"P${dayToPhase(day)}"
  }


}

class Nutritionist extends SpeechletV2{

  import Nutritionist._



  override def onSessionEnded(requestEnvelope: SpeechletRequestEnvelope[SessionEndedRequest]): Unit = {

  }

  override def onIntent(requestEnvelope: SpeechletRequestEnvelope[IntentRequest]): SpeechletResponse = {
    val intent =requestEnvelope.getRequest.getIntent
    requestEnvelope.getSession.getUser.getUserId
    intent.getName match {
      case "CanIEatThatIntent" =>
        val (food, phase) = intent.getSlots.foldLeft(("", currentPhase())){
          case (params,("Food", slot)) => params.copy(_1=slot.getValue)
          case (params,("Phase", slot)) => params.copy(_2=slot.getValue)
        }
    }

  }

  val t=Array("V", "F", "AP", "VP", "C", "G", "FT")

  def splitArr(arr: Array[String])=arr.foldLeft((Array.fill(7)(Seq[String]()),0)){
    case ((categories, categoryId), line) if line.toUpperCase==line =>
      (categories, categoryId + (if(categories(categoryId).isEmpty) 0 else 1))
    case ((categories, categoryId), line) =>
      categories(categoryId)=categories(categoryId):+line
      (categories, categoryId)
  }

  case class Food(name: String, kind: String, phase: String)

  def processRawData(s: String, phase: String) = {
    val (categoris, i) = splitArr(s.split("\n").filter(_.length>2))
    categoris.zipWithIndex.flatMap{case(names, i)=> names.map{Food(_,t(i), phase)}}
  }


  override def onLaunch(requestEnvelope: SpeechletRequestEnvelope[LaunchRequest]): SpeechletResponse = {

  }

  override def onSessionStarted(requestEnvelope: SpeechletRequestEnvelope[SessionStartedRequest]): Unit = {

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
