package nutritionist

/**
  * Created by sergeyzelvenskiy on 1/21/17.
  */
object DataPreprocessing {



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

  val preprocessed: Seq[(String,String, String)]=Seq.empty

  preprocessed.map{x=> x.copy(_1=x._1.replace(","," "), _3 = phaseProcess(x._3).mkString(","))}.map{case(name, kind, phase)=> s"$name,$kind,$phase"}


}
