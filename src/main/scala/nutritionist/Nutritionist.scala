package nutritionist

import com.amazon.speech.json.SpeechletRequestEnvelope
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet._
import com.amazon.speech.ui.{PlainTextOutputSpeech, Reprompt, SimpleCard}
import org.joda.time.{DateTime, DateTimeZone}

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

  val foods : Map[String, (String, Boolean, Boolean, Boolean)]  = Source.fromInputStream(this.getClass.getResourceAsStream("food.csv")).getLines().map(_.split(",")).collect{
    case Array(food, kind, p1, p2, p3) => food -> (kind, stringToBool(p1), stringToBool(p2), stringToBool(p3))
  }.toMap

  def lookupFood(food: String ): Option[(String, Boolean, Boolean, Boolean)]={
    val words = food.split(" ")
    if(words.length==1){
      foods.get(words(0))
    } else {
      None
    }
  }

  def 

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
        lookupFood(food) match {
          case Some((kind, p1, p2,p3))=>
            getSpeechletResponse("It looks like the food you are looking for is not on the list.", "", false)
          case None => getSpeechletResponse("It looks like the food you are looking for is not on the list.", "", false)
        }
      case _ => getSpeechletResponse("It looks like the food you are looking for is not on the list.", "", false)
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

  def phaseProcess(s: String)=s.split(",").map(_.replace("P", "").toInt-1).foldLeft(new Array[Int](3)){case(arr, index) =>
      arr(index)=1
      arr
    }

  val preprocessed: Seq[(String,String, String)]

  preprocessed.map{x=> x.copy(_1=x._1.replace(","," "), _3 = phaseProcess(x._3).mkString(","))}.map{case(name, kind, phase)=> s"$name,$kind,$phase"}



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
