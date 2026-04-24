package ec.edu.uisek.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CalculatorState(val display: String = "0")

sealed class CalculatorEvent {
    data class Number(val number: String) : CalculatorEvent()
    data class Operator(val operator: String) : CalculatorEvent()
    object Clear : CalculatorEvent()
    object AllClear : CalculatorEvent()
    object Calculate : CalculatorEvent()
    object Decimal : CalculatorEvent()
}

class CalculatorViewModel : ViewModel() {
    private var number1: String = ""
    private var number2: String = ""
    private var operator: String? = null

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


    private fun actualizarPantalla() {
        val opStr = operator ?: ""
        val textoCompleto = "$number1 $opStr $number2".trim()
        state = state.copy(display = if (textoCompleto.isEmpty()) "0" else textoCompleto)
    }

    private fun enterNumber(number: String) {
        if (operator == null) {
            number1 += number
        } else {
            number2 += number
        }
        actualizarPantalla()
    }

    private fun enterOperator(op: String) {
        if (number1.isNotBlank()) {
            if (number2.isNotBlank()) {
                performCalculation()
            }
            operator = op
            actualizarPantalla()
        }
    }

    private fun enterDecimal() {
        if (operator == null && !number1.contains(".")) {
            number1 += if (number1.isEmpty()) "0." else "."
        } else if (operator != null && !number2.contains(".")) {
            number2 += if (number2.isEmpty()) "0." else "."
        }
        actualizarPantalla()
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

            val resultString = if (result.isNaN()) "Error" else result.toString().removeSuffix(".0")
            number1 = if (result.isNaN()) "" else resultString
            number2 = ""
            operator = null

            state = state.copy(display = resultString)
        }
    }

    private fun clearLast() {
        if (number2.isNotEmpty()) {
            number2 = number2.dropLast(1)
        } else if (operator != null) {
            operator = null
        } else if (number1.isNotEmpty()) {
            number1 = number1.dropLast(1)
        }
        actualizarPantalla()
    }

    private fun clearAll() {
        number1 = ""
        number2 = ""
        operator = null
        state = state.copy(display = "0")
    }
}