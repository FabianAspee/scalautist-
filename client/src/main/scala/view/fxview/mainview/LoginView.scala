package view.fxview.mainview

import java.net.URL
import java.util.ResourceBundle

import controller.LoginController
import javafx.stage.Stage
import view.BaseView
import view.fxview.AbstractFXView

/**
 * A view to manage login functionalities.
 * It extends [[view.BaseView]]
 */
trait LoginView extends BaseView{
}

/**
 * Companion object of [[view.fxview.mainview.LoginView]]
 */
object LoginView{

  private class LoginViewFX(stage:Stage) extends AbstractFXView(stage) with LoginView{
    private var myController:LoginController = _

    override def show(): Unit =
      myStage show

    override def hide(): Unit =
      myStage hide

    override def close(): Unit =
      myStage close

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
      super.initialize(location,resources)
      myController = LoginController()
      myController.setView(this)
    }
  }

  def apply(stage:Stage):LoginView = new LoginViewFX(stage)
}
