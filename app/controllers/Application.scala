package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok( views.html.index() )
  }
  
  def compare = Action {
    Ok( views.html.compare() )
  }
  
  def review360 = Action {
    Ok( views.html.review360() )
  }
  
  def evaluation = Action {
    Ok( views.html.evaluation() )
  }
  
  def dashboard = Action {
    Ok( views.html.dashboard() )
  }

}