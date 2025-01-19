package com.geode.launcher.preferences

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.geode.launcher.R
import com.geode.launcher.ui.theme.GeodeLauncherTheme
import com.geode.launcher.ui.theme.Typography
import com.geode.launcher.utils.LabelledText
import com.geode.launcher.utils.PreferenceUtils
import kotlin.math.log10
import kotlin.math.roundToInt


fun toggleSetting(context: Context, preferenceKey: PreferenceUtils.Key): Boolean {
    val preferences = PreferenceUtils.get(context)

    return preferences.toggleBoolean(preferenceKey)
}

fun getSetting(context: Context, preferenceKey: PreferenceUtils.Key): Boolean {
    val preferences = PreferenceUtils.get(context)

    return preferences.getBoolean(preferenceKey)
}

@Composable
fun OptionsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = Typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        content()
    }
}

@Composable
fun SettingsSelectCard(
    title: String,
    dialogTitle: String,
    maxVal: Int,
    preferenceKey: PreferenceUtils.Key,
    toLabel: @Composable (Int) -> String,
    extraSelectBehavior: ((Int) -> Unit)? = null
) {
    val preferenceValue by PreferenceUtils.useIntPreference(preferenceKey)

    var showDialog by remember { mutableStateOf(false) }

    OptionsCard(
        title = { OptionsTitle(title = title) },
        modifier = Modifier
            .clickable(
                onClick = {
                    showDialog = true
                },
                role = Role.Button
            )
    ) {
        Text(toLabel(preferenceValue))
    }

    if (showDialog) {
        val context = LocalContext.current

        SelectDialog(
            title = dialogTitle,
            onDismissRequest = {
                showDialog = false
            },
            onSelect = { selected ->
                showDialog = false
                PreferenceUtils.get(context)
                    .setInt(preferenceKey, selected)
                extraSelectBehavior?.invoke(selected)
            },
            initialValue = preferenceValue,
        ) {
            (0..maxVal).forEach {
                SelectOption(name = toLabel(it), value = it)
            }
        }
    }
}

@Composable
fun SettingsStringSelectCard(
    title: String,
    dialogTitle: String,
    preferenceKey: PreferenceUtils.Key,
    options: Map<String, String>,
    extraSelectBehavior: ((String?) -> Unit)? = null
) {
    val preferenceValue by PreferenceUtils.useStringPreference(preferenceKey)

    var showDialog by remember { mutableStateOf(false) }

    OptionsCard(
        title = { OptionsTitle(title = title) },
        modifier = Modifier
            .clickable(
                onClick = {
                    showDialog = true
                },
                role = Role.Button
            )
    ) {
        Text(options[preferenceValue] ?: preferenceValue ?: "")
    }

    if (showDialog) {
        val context = LocalContext.current

        SelectDialog(
            title = dialogTitle,
            onDismissRequest = {
                showDialog = false
            },
            onSelect = { selected ->
                showDialog = false
                PreferenceUtils.get(context)
                    .setString(preferenceKey, selected)
                extraSelectBehavior?.invoke(selected)
            },
            initialValue = preferenceValue,
        ) {
            options.forEach { (k, v) ->
                SelectOption(name = v, value = k)
            }
        }
    }
}

@Composable
fun SettingsStringCard(
    title: String,
    dialogTitle: String,
    preferenceKey: PreferenceUtils.Key,
    filterInput: ((String) -> String)? = null
) {
    var preferenceValue by PreferenceUtils.useStringPreference(preferenceKey)

    var showDialog by remember { mutableStateOf(false) }

    OptionsCard(
        title = {
            OptionsTitle(title = title, description = preferenceValue)
        },
        modifier = Modifier
            .clickable(
                onClick = {
                    showDialog = true
                },
                role = Role.Button
            )
    ) { }

    if (showDialog) {
        StringDialog(
            title = dialogTitle,
            onDismissRequest = { showDialog = false },
            onSelect = {
                preferenceValue = it
                showDialog = false
            },
            initialValue = preferenceValue ?: "",
            filterInput = filterInput
        )
    }
}

@Composable
fun StringDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onSelect: (String) -> Unit,
    initialValue: String,
    filterInput: ((String) -> String)? = null
) {
    var enteredValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = enteredValue,
                onValueChange = {
                    enteredValue = filterInput?.invoke(it) ?: it
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    autoCorrectEnabled = false
                ),
                trailingIcon = {
                    if (enteredValue.isNotEmpty()) {
                        IconButton(onClick = { enteredValue = "" }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.preference_text_clear)
                            )
                        }
                    }
                }
            )
        },
        confirmButton = {
            TextButton(onClick = { onSelect(enteredValue) }) {
                Text(stringResource(R.string.message_box_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.message_box_cancel))
            }
        },
    )
}

@Composable
fun RangeDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onSelect: (Int) -> Unit,
    labelSuffix: String,
    initialValue: Int,
    range: IntRange,
    scale: Int,
    step: Int,
    children: @Composable () -> Unit
) {
    var enteredValue by remember {
        mutableFloatStateOf(initialValue / scale.toFloat())
    }

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(title)
        },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.0f, false)
                ) {
                    val precision = log10(scale.toFloat()).roundToInt()

                    Slider(
                        value = enteredValue,
                        onValueChange = { enteredValue = it },
                        valueRange = (range.first.toFloat()/scale)..(range.last.toFloat()/scale),
                        steps = ((range.last - range.first) - 1) / step,
                        modifier = Modifier.weight(1.0f)
                    )

                    Text("%.${precision}f$labelSuffix".format(enteredValue))
                }

                children()

            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect((enteredValue * scale).roundToInt()) }) {
                Text(stringResource(R.string.message_box_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.message_box_cancel))
            }
        },
    )
}

@Composable
fun SettingsRangeCard(
    title: String,
    dialogTitle: String,
    preferenceKey: PreferenceUtils.Key,
    labelSuffix: String,
    range: IntRange,
    scale: Int,
    step: Int,
    children: @Composable () -> Unit = {}
) {
    var preferenceValue by PreferenceUtils.useIntPreference(preferenceKey)

    var showDialog by remember { mutableStateOf(false) }

    OptionsCard(
        title = { OptionsTitle(title = title) },
        modifier = Modifier
            .clickable(
                onClick = {
                    showDialog = true
                },
                role = Role.Button
            )
    ) {
        val precision = log10(scale.toFloat()).roundToInt()
        val scaledValue = preferenceValue / scale.toFloat()

        Text("%.${precision}f$labelSuffix".format(scaledValue))
    }

    if (showDialog) {
        RangeDialog(
            title = dialogTitle,
            onDismissRequest = { showDialog = false },
            onSelect = {
                preferenceValue = it
                showDialog = false
            },
            initialValue = preferenceValue,
            range = range,
            scale = scale,
            labelSuffix = labelSuffix,
            step = step,
            children = children,
        )
    }
}

internal val LocalSelectValue = compositionLocalOf<Any> { 0 }
internal val LocalSelectSetValue = staticCompositionLocalOf<(Any) -> Unit> { {} }

@Composable
fun <T> SelectOption(name: String, value: T) {
    val currentValue = LocalSelectValue.current
    val setValue = LocalSelectSetValue.current

    // do not give the row or column padding!! it messes up the selection effect
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { setValue(value as Any) },
                role = Role.RadioButton
            )
            .padding(horizontal = 12.dp)
    ) {
        RadioButton(
            selected = currentValue.equals(value),
            onClick = { setValue(value as Any) }
        )
        Text(name, style = Typography.bodyMedium)
    }
}

@Composable
fun <T> SelectDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onSelect: (T) -> Unit,
    initialValue: T,
    options: @Composable () -> Unit,
) {
    val (selectedValue, setSelectedValue) = remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            // styling a dialog is actually a little hard if you're doing what i'm doing
            // maybe there's a better way to make these padding values...
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    title,
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(
                        start = 28.dp,
                        top = 24.dp,
                        bottom = 12.dp
                    )
                )

                CompositionLocalProvider(
                    LocalSelectValue provides (selectedValue as Any),
                    LocalSelectSetValue provides ({
                        @Suppress("UNCHECKED_CAST")
                        setSelectedValue(it as T)
                    })
                ) {
                    options()
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 16.dp,
                            end = 16.dp,
                            top = 4.dp
                        )
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.message_box_cancel))
                    }

                    TextButton(onClick = { onSelect(selectedValue) }) {
                        Text(stringResource(R.string.message_box_accept))
                    }
                }
            }
        }
    }
}

@Composable
fun OptionsButton(title: String, description: String? = null, icon: (@Composable () -> Unit)? = null, displayInline: Boolean = false, onClick: () -> Unit) {
    OptionsCard(
        title = {
            OptionsTitle(
                title = title,
                description = description.takeIf { !displayInline },
                icon = icon
            )
        },
        modifier = Modifier
            .clickable(onClick = onClick, role = Role.Button)
    ) {
        if (displayInline && description != null) {
            Text(description, textAlign = TextAlign.End)
        }
    }
}

@Composable
fun SettingsCard(title: String, description: String? = null, icon: (@Composable () -> Unit)? = null, asCard: Boolean = true, preferenceKey: PreferenceUtils.Key) {
    val context = LocalContext.current
    val settingEnabled = remember {
        mutableStateOf(getSetting(context, preferenceKey))
    }

    OptionsCard(
        title = {
            OptionsTitle(
                Modifier.fillMaxWidth(0.75f),
                title = title,
                description = description,
                icon = icon
            )
        },
        modifier = if (asCard) Modifier.toggleable(
            value = settingEnabled.value,
            onValueChange = { settingEnabled.value = toggleSetting(context, preferenceKey) },
            role = Role.Switch,
        ) else Modifier
    ) {
        Switch(
            checked = settingEnabled.value,
            onCheckedChange = if (!asCard)
                { _ -> settingEnabled.value = toggleSetting(context, preferenceKey) }
            else null
        )
    }
}

@Composable
fun OptionsTitle(modifier: Modifier = Modifier, title: String, description: String? = null, icon: (@Composable () -> Unit)? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) {
            icon()
        }
        Column(
            modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title)
            if (!description.isNullOrEmpty()) {
                Text(
                    description,
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun OptionsCard(modifier: Modifier = Modifier, title: @Composable () -> Unit, content: @Composable () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,
    ) {
        title()
        content()
    }
}

@Composable
fun InlineText(label: String, icon: @Composable (() -> Unit)? = null, modifier: Modifier = Modifier) {
    LabelledText(label = label, icon = icon, modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}

@Preview(showBackground = true)
@Composable
fun OptionsCardPreview() {
    GeodeLauncherTheme {
        OptionsGroup(title = "Preview Group") {
            SettingsCard(
                title = "Load files from /test",
                description = "Very long testing description goes here. It is incredibly long, it should wrap onto a new line.",
                preferenceKey = PreferenceUtils.Key.BLACK_BACKGROUND
            )
            SettingsCard(
                title = "Testing option 2",
                preferenceKey = PreferenceUtils.Key.LOAD_AUTOMATICALLY
            )
        }
    }
}
