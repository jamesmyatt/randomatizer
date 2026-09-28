package io.github.jamesmyatt.randomatizer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jamesmyatt.randomatizer.BuildConfig
import io.github.jamesmyatt.randomatizer.R
import io.github.jamesmyatt.randomatizer.color.DieStyle
import io.github.jamesmyatt.randomatizer.color.dieStyle
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.DiceFace
import io.github.jamesmyatt.randomatizer.settings.Mode
import io.github.jamesmyatt.randomatizer.settings.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SettingsContent(settings, onUpdate)
    }
}

/** The settings sheet's contents, separate from the sheet so screenshot tests can render it. */
@Composable
internal fun SettingsContent(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)
        SwitchRow(stringResource(R.string.advanced_mode), settings.mode == Mode.Advanced) { on ->
            onUpdate { it.withMode(if (on) Mode.Advanced else Mode.Basic) }
        }
        SwitchRow(stringResource(R.string.show_total), settings.showTotal) { show ->
            onUpdate { it.copy(showTotal = show) }
        }
        ThemeSection(settings.themeMode) { mode -> onUpdate { it.copy(themeMode = mode) } }
        DiceColorSection(settings) { face -> onUpdate { it.copy(diceFace = face) } }
        if (settings.diceFace == DiceFace.Custom) {
            CustomFaceEditor(settings.customFace) { argb -> onUpdate { it.copy(customFace = argb) } }
        }
        Text(
            text = stringResource(R.string.about, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, onValueChange = onChange, role = Role.Switch),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.surface,
                checkedTrackColor = colors.onSurface,
                checkedBorderColor = colors.onSurface,
            ),
        )
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 14.dp).weight(1f),
        )
        trailing()
    }
}

@Composable
private fun ThemeSection(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(Modifier.selectableGroup()) {
        SectionTitle(stringResource(R.string.theme))
        ThemeMode.entries.forEach { mode ->
            val label = when (mode) {
                ThemeMode.System -> R.string.theme_system
                ThemeMode.Light -> R.string.light
                ThemeMode.Dark -> R.string.dark
            }
            RadioRow(stringResource(label), selected = mode == selected, onClick = { onSelect(mode) })
        }
    }
}

@Composable
private fun DiceColorSection(settings: AppSettings, onSelect: (DiceFace) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.selectableGroup()) {
        SectionTitle(stringResource(R.string.dice_color))
        DiceFace.entries.forEach { face ->
            val label = when (face) {
                DiceFace.Background -> R.string.dice_color_background
                DiceFace.Foreground -> R.string.dice_color_foreground
                DiceFace.Custom -> R.string.dice_color_custom
            }
            RadioRow(stringResource(label), selected = face == settings.diceFace, onClick = { onSelect(face) }) {
                NumberDie(
                    die = StandardDie.D6,
                    value = 5,
                    style = dieStyle(face, settings.customFace, colors.surface.toArgb(), colors.onSurface.toArgb()),
                    size = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun CustomFaceEditor(customFace: Int, onPick: (Int) -> Unit) {
    val context = LocalContext.current
    val light = remember(context) { dynamicLightColorScheme(context) }
    val dark = remember(context) { dynamicDarkColorScheme(context) }
    val onLight = dieStyle(DiceFace.Custom, customFace, light.surface.toArgb(), light.onSurface.toArgb())
    val onDark = dieStyle(DiceFace.Custom, customFace, dark.surface.toArgb(), dark.onSurface.toArgb())

    Column {
        SwatchGrid(selected = customFace, onPick = onPick)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 10.dp)) {
            BackgroundPreview(onLight, light, stringResource(R.string.light), Modifier.weight(1f))
            BackgroundPreview(onDark, dark, stringResource(R.string.dark), Modifier.weight(1f))
        }
        val worst = minOf(onLight.pipsContrast, onDark.pipsContrast)
        if (onLight.lowPipsContrast || onDark.lowPipsContrast) {
            Warning(stringResource(R.string.warning_face_pips, "%.1f".format(worst)))
        }
    }
}

@Composable
private fun SwatchGrid(selected: Int, onPick: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        maxItemsInEachRow = 6,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).selectableGroup(),
    ) {
        DicePalette.forEach { swatch ->
            val isSelected = swatch.argb == selected
            val name = stringResource(swatch.name)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onPick(swatch.argb) })
                    .semantics { contentDescription = name },
            ) {
                Canvas(Modifier.size(40.dp)) {
                    drawCircle(Color(swatch.argb))
                    drawCircle(colors.outline, style = Stroke(1.dp.toPx()))
                }
                if (isSelected) {
                    Box(Modifier.size(48.dp).border(3.dp, colors.onSurface, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun BackgroundPreview(style: DieStyle, scheme: ColorScheme, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .height(64.dp)
            .background(scheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        NumberDie(die = StandardDie.D6, value = 5, style = style, size = 40.dp)
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurface)
    }
}

@Composable
private fun Warning(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = colors.onErrorContainer)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.onErrorContainer)
    }
}
