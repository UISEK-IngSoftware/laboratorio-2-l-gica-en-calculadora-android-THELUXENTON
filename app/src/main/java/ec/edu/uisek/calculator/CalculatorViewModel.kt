package ec.edu.uisek.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Estado visible en la interfaz
data class CalculatorState(val display: String = "0")

// Acciones permitidas en la calculadora
sealed class CalculatorEvent {
    data class Number(val number: String) : CalculatorEvent()
    data class Operator(val operator: String) : CalculatorEvent()
    object Clear : CalculatorEvent()
    object AllClear : CalculatorEvent()
    object Calculate : CalculatorEvent()
    object Decimal : CalculatorEvent()
}

class CalculatorViewModel : ViewModel() {

    // Variables internas de la lógica del profe
    private var number1: String = ""
    private var number2: String = ""
    private var operator: String? = null
    private var isResultOnScreen = false

    var state by mutableStateOf(CalculatorState())
        private set

    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.Number -> enterNumber(event.number)
            is CalculatorEvent.Operator -> enterOperator(event.operator)
            is CalculatorEvent.Decimal -> enterDecimal()
            is CalculatorEvent.AllClear -> clearAll()
            is CalculatorEvent.Clear -> clearLast()
            is CalculatorEvent.Calculate -> performCalculation()
        }
    }

    // --- MAGIA VISUAL: Junta los números y el operador en la pantalla ---
    private fun actualizarPantalla() {
        val opStr = operator ?: ""
        // Creamos un texto que se ve así: "2 + 3"
        val textoCompleto = "$number1 $opStr $number2".trim()
        state = state.copy(display = if (textoCompleto.isEmpty()) "0" else textoCompleto)
    }

    private fun enterNumber(number: String) {
        if (operator == null) {
            if (isResultOnScreen) {
                number1 = ""
                isResultOnScreen = false
            }
            number1 += number
        } else {
            number2 += number
        }
        actualizarPantalla() // Actualizamos la vista
    }

    private fun enterOperator(op: String) {
        if (number1.isNotBlank()) {
            // Si ya hay un segundo número y ponemos otro signo, calculamos el intermedio
            if (number2.isNotBlank()) {
                performCalculation()
            }
            operator = op
            isResultOnScreen = false
            actualizarPantalla() // Mostramos el signo en pantalla
        }
    }

    private fun enterDecimal() {
        if (operator == null) {
            if (!number1.contains(".")) {
                if (isResultOnScreen) {
                    number1 = ""
                    isResultOnScreen = false
                }
                number1 += "."
            }
        } else {
            if (!number2.contains(".")) {
                number2 += "."
            }
        }
        actualizarPantalla() // Mostramos el punto
    }

    private fun performCalculation() {
        val num1 = number1.toDoubleOrNull()
        val num2 = number2.toDoubleOrNull()

        if (num1 != null && num2 != null && operator != null) {
            val result = when (operator) {
                "+" -> num1 + num2
                "−", "-" -> num1 - num2
                "×", "*" -> num1 * num2
                "÷", "/" -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> 0.0
            }

            clearAll()

            val resultString = if (result.isNaN()) "Error" else result.toString().removeSuffix(".0")
            number1 = if (result.isNaN()) "" else resultString

            state = state.copy(display = resultString)
            isResultOnScreen = true
        }
    }

    private fun clearLast() {
        if (operator == null) {
            if (number1.isNotBlank()) {
                number1 = number1.dropLast(1)
            }
            isResultOnScreen = false
        } else {
            if (number2.isNotBlank()) {
                number2 = number2.dropLast(1)
            } else {
                operator = null
            }
        }
        actualizarPantalla() // Actualizamos después de borrar
    }

    private fun clearAll() {
        number1 = ""
        number2 = ""
        operator = null
        isResultOnScreen = false
        state = state.copy(display = "0")
    }
}