package dev.mcd.approachminima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ChipDefaults
import androidx.compose.material.FilterChip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.mcd.approachminima.ui.theme.ApproachMinimaTheme
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    private data class State(
        val approach: Approach? = null,
        val adElevation: Int = 0,
        val thresholdElevation: Int = 0,
        val och: Int = 0,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(State()) }

            ApproachMinimaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    LazyColumn {
                        item {
                            Column {
                                SectionLabel(text = "System")

                                FlowRow {
                                    approaches.forEach {
                                        ApproachChip(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            name = it.name,
                                            isSelected = it == state.approach,
                                        ) {
                                            state = state.copy(approach = it)
                                        }
                                    }
                                }

                                SectionLabel(text = "Procedure")

                                ProcedureInputFields { adElevation: Int, thresholdElevation: Int, och: Int ->
                                    state = state.copy(
                                        adElevation = adElevation,
                                        thresholdElevation = thresholdElevation,
                                        och = och,
                                    )
                                }

                                SectionLabel(text = "Minima")

                                OutputView(state = state)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OutputView(state: State) {
        val greenSpanStyle = SpanStyle(
            color = Color.Green,
        )
        val magentaSpanStyle = SpanStyle(
            color = Color.Magenta,
        )
        val cyanSpanStyle = SpanStyle(
            color = Color.Cyan,
        )

        fun Int.magentaFtSpan() = AnnotatedString("$this ft", magentaSpanStyle)
        fun Int.cyanFtSpan() = AnnotatedString("$this ft", cyanSpanStyle)

        val approach = state.approach ?: return
        var minimum: Int
        val result = buildAnnotatedString {
            append(AnnotatedString(approach.name, greenSpanStyle))
            append(" has a system minima of ")
            append(approach.systemMinima.cyanFtSpan())

            minimum = if (state.och > approach.systemMinima) {
                append(", which is less than the OCH of ")
                appendLine(state.och.cyanFtSpan())
                state.och
            } else {
                append(", which is greater than the OCH of ")
                appendLine(state.och.cyanFtSpan())
                approach.systemMinima
            }

            append("> ")
            appendLine(minimum.magentaFtSpan())
            appendLine()

            if (approach.precision) {
                append("Add ")
                append(50.cyanFtSpan())
                appendLine(" for the Pressure Error Correction")
                minimum += 50
                append("> ")
                appendLine(minimum.magentaFtSpan())
                appendLine()
            }

            append("Add the recommended ")
            append(200.cyanFtSpan())
            appendLine(" for the IR(R)")
            minimum += 200
            append("> ")
            appendLine(minimum.magentaFtSpan())
            appendLine()

            minimum = if (approach.precision) {
                if (minimum < 500) {
                    append("Which is below than the recommended ")
                    append(500.cyanFtSpan())
                    appendLine(" for IR(R) precision approaches")
                    500
                } else {
                    append("Which is above than the recommended ")
                    append(500.cyanFtSpan())
                    appendLine(" for IR(R) precision approaches")
                    minimum
                }
            } else {
                if (minimum < 600) {
                    append("Which is below than the recommended ")
                    append(600.cyanFtSpan())
                    appendLine(" for IR(R) non-precision approaches")
                    600
                } else {
                    append("Which is above than the recommended ")
                    append(600.cyanFtSpan())
                    appendLine(" for IR(R) non-precision approaches")
                    minimum
                }
            }

            append("> ")
            appendLine(minimum.magentaFtSpan())
            appendLine()

            val elevation = if (approach.precision) {
                state.thresholdElevation
            } else {
                if (state.thresholdElevation < state.adElevation) {
                    if (abs(state.thresholdElevation - state.adElevation) > 7) {
                        state.thresholdElevation
                    } else {
                        state.adElevation
                    }
                } else {
                    state.adElevation
                }
            }

            val decisionHeightSpan = SpanStyle(textDecoration = TextDecoration.Underline)

            if (approach.precision) {
                appendLine(AnnotatedString("DH $minimum ft", decisionHeightSpan))
                appendLine(AnnotatedString("DA ${elevation + minimum} ft", decisionHeightSpan))
            } else {
                appendLine(AnnotatedString("MDH $minimum ft", decisionHeightSpan))
                appendLine(AnnotatedString("MDA ${elevation + minimum} ft", decisionHeightSpan))
            }
        }

        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = result
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun ProcedureInputFields(
        onValuesUpdated: (adElevation: Int, thresholdElevation: Int, och: Int) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardManager = LocalSoftwareKeyboardController.current
        var adElevation by remember { mutableStateOf<Int?>(null) }
        var thresholdElevation by remember { mutableStateOf<Int?>(null) }
        var och by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(adElevation, thresholdElevation, och) {
            onValuesUpdated(
                adElevation ?: return@LaunchedEffect,
                thresholdElevation ?: return@LaunchedEffect,
                och ?: return@LaunchedEffect,
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                label = { Text(text = "AD Elev.") },
                value = adElevation?.toString() ?: "",
                onValueChange = {
                    adElevation = it.toIntOrNull()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Next
                    )
                })
            )

            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                label = { Text(text = "THR Elev.") },
                value = thresholdElevation?.toString() ?: "",
                onValueChange = {
                    thresholdElevation = it.toIntOrNull()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Next
                    )
                })
            )

            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                label = { Text(text = "OCH") },
                value = och?.toString() ?: "",
                onValueChange = {
                    och = it.toIntOrNull()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardManager?.hide()
                    focusManager.clearFocus(force = true)
                })
            )
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = text
        )
    }

    @Composable
    private fun ApproachChip(
        modifier: Modifier = Modifier,
        name: String,
        isSelected: Boolean,
        onClick: () -> Unit = {},
    ) {
        FilterChip(
            modifier = modifier,
            onClick = onClick,
            selected = isSelected,
            colors = ChipDefaults.filterChipColors(
                selectedBackgroundColor = Color.Magenta
            )
        ) {
            Text(text = name)
        }
    }
}