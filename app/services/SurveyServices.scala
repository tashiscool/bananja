package services

import java.util.UUID

import models.dao.sapi.{Survey, Category, LineItem}
import play.api.Configuration
import scala.collection.JavaConverters._

/**
 * Created by tashdid.khan on 7/10/2015.
 */
object SurveyServices {
  def getServey(conf: Configuration) ={
    val configs = conf.getConfigList("surveys").get
    val serveys = configs.asScala.map { config =>
      val id = UUID.randomUUID().toString.replaceAll("-","")
      config.getConfigList("categorical").getOrElse(List.empty[Configuration].asJava).asScala.map{ categoricalList =>
        Survey(id, config2Map(categoricalList), config.getString("name").getOrElse(""))
      }.toList
    }.toList.flatten
    serveys
  }
  def config2LineItem(conf: Configuration) = {
    val id = UUID.randomUUID().toString.replaceAll("-","")
    LineItem(id, conf.getInt("min").getOrElse(0), conf.getInt("max").getOrElse(10), conf.getString("question").getOrElse(""))
  }
  def config2Map(conf: Configuration):Map[Category,List[LineItem]] = {
    Map((Category(conf.getString("category").getOrElse("")),conf.getConfigList("lineItems").getOrElse(List.empty[Configuration].asJava).asScala.map(config2LineItem(_)).toList))
  }
}
