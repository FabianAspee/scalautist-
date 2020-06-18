package utils

import java.sql.Date
import java.time.temporal.ChronoUnit
import java.util.Calendar

/**
 * Helper object to work with [[java.sql.Date]]
 */
object DateConverter {
  /**
   * Returns the first date of the month provided
   */
  val startMonthDate: Date => Date = date  =>
    converter(date,c => {
      c.set(Calendar.DAY_OF_MONTH,1)
      c
    })

  /**
   * Returns the first date of the month after the provided
   */
  val nextMonthDate: Date =>  Date = date =>
    converter(date,c =>{
      c.add(Calendar.MONTH,1)
      c.set(Calendar.DAY_OF_MONTH,1)
      c
    })


  /**
   * Returns the last date of the month provided
   */
  val endOfMonth: Date => Date = date =>
    converter(date,c => {
      c.set(Calendar.DAY_OF_MONTH,Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH))
      c
    })

  /**
   * Return the first date of the year getting the year as input
   */
  val dateFromYear: Int => Date = year => {
    val calendar = Calendar.getInstance()
    calendar.set(year,0,1)
    new Date(calendar.getTimeInMillis)
  }

  /**
   * Returns the number of days between two dates.
   * It Counts the starting day as a whole day, i.e. between the same day there is 1 day.
   */
  val computeDaysBetweenDates: (Date,Date) => Int = (dateStart,dateStop) =>
    ChronoUnit.DAYS.between(dateStart.toLocalDate,dateStop.toLocalDate).toInt + 1

  /**
   * Returns whether or not two date have different year
   */
  val notSameYear: (Date,Date) => Boolean = (start,end) =>
    start.toLocalDate.getYear != end.toLocalDate.getYear

  private val converter: (Date, Calendar => Calendar) => Date = (date, function) =>{
    var calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar = function(calendar)
    new Date(calendar.getTimeInMillis)
  }

  val getWeekNumber:Date=>Int = date=>{
    val calendar = Calendar.getInstance()
    calendar.setTime(date)
    calendar.get(Calendar.WEEK_OF_YEAR)
  }
}
