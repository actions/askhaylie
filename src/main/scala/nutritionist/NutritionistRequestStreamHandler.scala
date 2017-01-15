package nutritionist

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
  * Created by sergeyzelvenskiy on 1/14/17.
  */
class NutritionistRequestStreamHandler extends SpeechletRequestStreamHandler(new Nutritionist, Set("amzn1.ask.skill.c43c9d81-fd52-4601-8fcb-bb346589e842").asJava){
    /*
            * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
            * Alexa Skill and put the relevant Application Ids in this Set.
            */
    val supportedApplicationIds = Set("amzn1.ask.skill.c43c9d81-fd52-4601-8fcb-bb346589e842")

}
