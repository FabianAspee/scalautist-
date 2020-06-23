package dbfactory.operation

import java.sql.Date

import caseclass.CaseClassDB.{Risultato, Turno}
import caseclass.CaseClassHttpMessage.{InfoHome, InfoShift, ShiftDay}
import dbfactory.implicitOperation.ImplicitInstanceTableDB.{InstancePersona, InstanceRisultato, InstanceTurno}
import dbfactory.implicitOperation.OperationCrud
import dbfactory.setting.Table.{RisultatoTableQuery, TurnoTableQuery}
import dbfactory.util.Helper._
import messagecodes.StatusCodes
import slick.jdbc.SQLServerProfile.api._
import utils.DateConverter

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
 * @author Francesco Cassano
 *
 * Allows to perform operation on RisultatoSet table
 */
trait RisultatoOperation extends OperationCrud[Risultato]{

  /**
   * It returns data on an employee's daily work shifts
   *
   * @param idUser
   *               employee id
   * @param date
   *             day to return
   * @return
   *         Future of Option of [[InfoHome]] that contains information about shift
   */
  def getTurniInDate(idUser: Int, date: Date): Future[Option[InfoHome]]

  /**
   * Returns all data on an employee's work shifts in the next 7 days from selected date
   *
   * @param idUser
   *               employee id
   * @param date
   *             initial day for week count
   * @return
   *         Future of Option of [[InfoShift]] that contains information on shifts of the week
   */
  def getTurniSettimanali(idUser: Int, date: Date): Future[Option[InfoShift]]

  /**
   * Method which allow update absence of the driver with another driver that contains availability.
   * may be verified if driver that are received, really contains availability in this idRisultato
   *
   * @param idRisultato represent point where absence exist
   * @param idPersona person that replace the driver absenced
   * @return Future of Option with status code of operation
   *         [[messagecodes.StatusCodes.SUCCES_CODE]] if operation of update finish with success
   *         [[messagecodes.StatusCodes.ERROR_CODE1]] if idRisultato not exist
   *         [[messagecodes.StatusCodes.ERROR_CODE2]] if idPersona not exist
   */
  def updateAbsence(idRisultato:Int,idPersona:Int):Future[Option[Int]]
}

object RisultatoOperation extends RisultatoOperation {
  def verifyResult(idRisultato: Int): Future[Option[Int]] = {
    select(idRisultato).collect{
      case Some(_) => Option(StatusCodes.SUCCES_CODE)
      case None =>Option(StatusCodes.ERROR_CODE1)
    }
  }


  private case class Shift(day: Date, name: String)

  override def getTurniInDate(idUser: Int, date: Date): Future[Option[InfoHome]] = {
    for{
      listTurni <- InstanceRisultato.operation().execQueryFilter(field => field.turnoId,
        filter => filter.data === date && filter.personeId === idUser)
      turni <- InstanceTurno.operation().selectFilter(filter => filter.id.inSet(listTurni.getOrElse(List.empty[Int])))
      disponibilita <- DisponibilitaOperation.getDisponibilita(idUser, DateConverter.getWeekNumber(date))
    } yield if(disponibilita.isDefined)
      Some(InfoHome(turni.getOrElse(List.empty[Turno]), disponibilita.get))
    else
      None
  }

  override def getTurniSettimanali(idUser: Int, date: Date): Future[Option[InfoShift]] = {
    val dateEnd = DateConverter.nextWeek(date)

    for{
      turni <- getTurnoOfDays(idUser, date, dateEnd)
      disponibilita <- DisponibilitaOperation.getDisponibilita(idUser, DateConverter.getWeekNumber(date))
    } yield if(disponibilita.isDefined && turni.isDefined)
      Some(InfoShift(turni.get.map(turno => ShiftDay(turno.day.toLocalDate.getDayOfMonth, turno.name)), disponibilita.get))
    else
      None
  }

  /**
   * Returns turno of the day in a week
   *
   * @param idUser
   *               employee
   * @param initDate
   * @param endDate
   * @return
   */
  private def getTurnoOfDays(idUser: Int, initDate: Date, endDate: Date): Future[Option[List[Shift]]] = {
    val filter = for{
      turni <- RisultatoTableQuery.tableQuery() join TurnoTableQuery.tableQuery() on (_.turnoId ===_.id)
              if turni._1.data >= initDate && turni._1.data < endDate && turni._1.id === idUser
    } yield (turni._1.data, turni._2.nomeTurno)

    InstanceRisultato.operation().execJoin(filter).collect{
      case Some(shifts) => Some(shifts.map(shift => Shift(shift._1, shift._2)))
      case _ => None
    }
  }

  override def updateAbsence(idRisultato:Int,idPersona:Int):Future[Option[Int]]=
    for{
      result <- InstanceRisultato.operation().execQueryFilter(risultato=>risultato.id,
        risultato=>risultato.id===idRisultato)
      persona <- InstancePersona.operation().execQueryFilter(persona=>persona.id,persona=>persona.id===idPersona)
      finalResult<- (result,persona) match{
        case (None, _) => Future.successful(Some(StatusCodes.ERROR_CODE1))
        case (_, None) => Future.successful(Some(StatusCodes.ERROR_CODE2))
        case (_, _) => update(idRisultato,idPersona)
      }
    }yield finalResult

  private def update(idRisultato:Int,idPersona:Int)={
    InstanceRisultato.operation().execQueryUpdate(risultato=>risultato.personeId,risultato=>risultato.id===idRisultato,
      idPersona).result()
  }
}
