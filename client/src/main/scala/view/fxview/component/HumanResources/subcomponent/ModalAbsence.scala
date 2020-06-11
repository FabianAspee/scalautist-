package view.fxview.component.HumanResources.subcomponent

import java.net.URL
import java.util.ResourceBundle

import caseclass.CaseClassDB.Assenza
import caseclass.CaseClassHttpMessage.Ferie
import javafx.fxml.FXML
import javafx.scene.control.{Button, DatePicker, TextField}
import view.fxview.component.HumanResources.subcomponent.parent.ModalAbsenceParent
import view.fxview.component.HumanResources.subcomponent.util.CreateDatePicker
import view.fxview.component.HumanResources.subcomponent.util.CreateDatePicker.MoveDatePeriod
import view.fxview.component.{AbstractComponent, Component}

/**
 * @author Fabian Aspee Encina
 *
 * Interface used for communicate with the view. It extends [[view.fxview.component.Component]]
 * * of [[view.fxview.component.HumanResources.subcomponent.parent.ModalAbsenceParent]]
 */
trait ModalAbsence extends Component[ModalAbsenceParent]{

}

/**
 * Companion object of [[view.fxview.component.HumanResources.subcomponent.ModalAbsence]]
 *
 */
object ModalAbsence{

  def apply(item:Ferie, isMalattia:Boolean=true): ModalAbsence =new ModalAbsenceFX(item,isMalattia)

  /**
   * javaFX private implementation of [[view.fxview.component.HumanResources.subcomponent.ModalAbsence]]
   *
   * @param item
   *             instance of [[caseclass.CaseClassHttpMessage.Ferie]] about employee's available free days
   * @param isMalattia
   *                   boolean to specify if day off is sickness or vacation
   */
  private class ModalAbsenceFX(item:Ferie, isMalattia:Boolean)
    extends AbstractComponent[ModalAbsenceParent]("humanresources/subcomponent/ModalAbsence")
      with ModalAbsence {

    @FXML
    var nameSurname:TextField = _
    @FXML
    var initDate:DatePicker = _
    @FXML
    var finishDate:DatePicker = _
    @FXML
    var button:Button = _

    override def initialize(location: URL, resources: ResourceBundle): Unit = {
      if(isMalattia)button.setText(resources.getString("absence-button")) else button.setText(resources.getString("holiday-button"))
      nameSurname.setText(s"${item.nomeCognome}")
      setInitDate()
      initDate.setOnAction(_=>enableFinishDate())
      finishDate.setOnAction(_=>enableButton())
      button.setOnAction(_=>saveAbscence())

    }
    private def saveAbscence(): Unit ={
      parent.saveAbsence(Assenza(
                item.idPersona,
                CreateDatePicker.createDataSql(initDate),
                CreateDatePicker.createDataSql(finishDate),
                isMalattia)
      )
    }

    private def enableFinishDate(): Unit ={
      var sickness: Int = 0
      var holiday: Int = 0
      if(isMalattia) sickness = 3
      else
        holiday = 30-item.giorniVacanza
      CreateDatePicker.createDataPicker(finishDate, MoveDatePeriod(), MoveDatePeriod(years = sickness, days = holiday), initDate.getValue)
    }

    private def setInitDate(): Unit ={
      CreateDatePicker.createDataPicker(initDate, MoveDatePeriod(days = 3),MoveDatePeriod(years = 5))
    }

    private def enableButton(): Unit ={
      button.setDisable(false)
    }


  }
}
