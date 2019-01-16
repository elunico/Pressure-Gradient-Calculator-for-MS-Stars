package pgc

import java.lang.reflect.InvocationTargetException

/**
 * @author Thomas Povinelli
 * Created 2019-Jan-16
 * In Pressure-Gradient-Calculator-for-MS-Stars
 */
class InputData(
  limitText: String, radiusText: String,
  densityText: String, stepText: String
) {

  private val limitText: String = limitText.trim { it <= ' ' }
  private val radiusText: String = radiusText.trim { it <= ' ' }
  private val densityText: String = densityText.trim { it <= ' ' }
  private val stepText: String = stepText.trim { it <= ' ' }

  data class Parameters(
    val limit: Int, val radius: Long,
    val steps: Long, val density: Double
  )

  fun parse(): Parameters? {
    val limit = parseInputText(limitText, 45, "sections") ?: return null
    val radius = parseInputText(radiusText, 23193333L, "radius") ?: return null
    val steps = parseInputText(stepText, 45000000L, "steps") ?: return null
    val density = parseInputText(densityText, 4.5, "density") ?: return null
    return Parameters(limit, radius, steps, density)
  }

  private inline fun <reified T : Number> parseInputText(
    inputText: String,
    defaultValue: T,
    fieldName: String
  ): T? {
    return if (inputText == "") {
      defaultValue
    } else {
      try {
        val p = T::class.java.getDeclaredMethod("parse${T::class.java.simpleName.fixMe()}", String::class.java)
        p.invoke(null, inputText) as T
      } catch (e: InvocationTargetException) {
        Controller.displayAlert("Please enter a valid " + T::class.java.simpleName + " for " + fieldName)
        return null
      }
    }
  }

  // WHY IS parseInt THE *ONLY* METHOD THAT DOES NOT END IN ITS CLASS NAME?!
  private fun String.fixMe(): String = if (this == "Integer") "Int" else this
}


