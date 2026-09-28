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
import io.github.jamesmyatt.randomatizer.color.checkDiceColors
import io.github.jamesmyatt.randomatizer.color.dieStyle
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.CustomDiceColors
import io.github.jamesmyatt.randomatizer.settings.DiceColorMode
import io.github.jamesmyatt.randomatizer.settings.Mode

private enum class ColorTarget { Face, Pips }

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
        DiceColorSection(settings) { mode -> onUpdate { it.copy(diceColorMode = mode) } }
        if (settings.diceColorMode == DiceColorMode.Custom) {
            CustomColorsEditor(
                custom = settings.customColors,
                onPickFace = { argb ->
                    onUpdate { s ->
                        if (argb == s.customColors.pips) s else s.copy(customColors = s.customColors.copy(face = argb))
                    }
                },
                onPickPips = { argb ->
                    onUpdate { s ->
                        if (argb == s.customColors.face) s else s.copy(customColors = s.customColors.copy(pips = argb))
                    }
                },
            )
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
private fun DiceColorSection(settings: AppSettings, onSelect: (DiceColorMode) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.selectableGroup()) {
        SectionTitle(stringResource(R.string.dice_colors))
        DiceColorMode.entries.forEach { mode ->
            val selected = mode == settings.diceColorMode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .selectable(selected = selected, onClick = { onSelect(mode) }, role = Role.RadioButton),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = colors.onSurface),
                )
                Text(
                    text = stringResource(
                        when (mode) {
                            DiceColorMode.System -> R.string.dice_colors_system
                            DiceColorMode.SystemInverted -> R.string.dice_colors_system_inverted
                            DiceColorMode.Custom -> R.string.dice_colors_custom
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 14.dp).weight(1f),
                )
                NumberDie(
                    die = StandardDie.D6,
                    value = 5,
                    style = dieStyle(mode, settings.customColors, colors.surface.toArgb(), colors.onSurface.toArgb()),
                    size = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun CustomColorsEditor(custom: CustomDiceColors, onPickFace: (Int) -> Unit, onPickPips: (Int) -> Unit) {
    var open by rememberSaveable { mutableStateOf<ColorTarget?>(null) }
    val context = LocalContext.current
    val light = remember(context) { dynamicLightColorScheme(context) }
    val dark = remember(context) { dynamicDarkColorScheme(context) }

    Column {
        ColorRow(stringResource(R.string.face), custom.face) {
            open =
                if (open == ColorTarget.Face) null else ColorTarget.Face
        }
        if (open == ColorTarget.Face) {
            SwatchGrid(
                selected = custom.face,
                unavailable = custom.pips,
                unavailableLabel = R.string.swatch_same_as_pips,
                onPick = onPickFace,
            )
        }
        ColorRow(stringResource(R.string.pips), custom.pips) {
            open =
                if (open == ColorTarget.Pips) null else ColorTarget.Pips
        }
        if (open == ColorTarget.Pips) {
            SwatchGrid(
                selected = custom.pips,
                unavailable = custom.face,
                unavailableLabel = R.string.swatch_same_as_face,
                onPick = onPickPips,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 10.dp)) {
            BackgroundPreview(custom, light, stringResource(R.string.light), Modifier.weight(1f))
            BackgroundPreview(custom, dark, stringResource(R.string.dark), Modifier.weight(1f))
        }
        ColorWarnings(custom, light, dark)
    }
}

@Composable
private fun ColorRow(label: String, argb: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(Color(argb), CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = colorName(argb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun colorName(argb: Int): String = DicePalette.firstOrNull { it.argb == argb }?.let { stringResource(it.name) }
    ?: "#%06X".format(argb and 0xFFFFFF)

@Composable
private fun SwatchGrid(selected: Int, unavailable: Int, unavailableLabel: Int, onPick: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    FlowRow(
        maxItemsInEachRow = 6,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).selectableGroup(),
    ) {
        DicePalette.forEach { swatch ->
            val isSelected = swatch.argb == selected
            val isUnavailable = swatch.argb == unavailable
            val name = stringResource(swatch.name)
            val description = if (isUnavailable) stringResource(unavailableLabel, name) else name
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .selectable(
                        selected = isSelected,
                        enabled = !isUnavailable,
                        role = Role.RadioButton,
                        onClick = { onPick(swatch.argb) },
                    )
                    .semantics { contentDescription = description },
            ) {
                Canvas(Modifier.size(40.dp).alpha(if (isUnavailable) 0.35f else 1f)) {
                    drawCircle(Color(swatch.argb))
                    drawCircle(colors.outline, style = Stroke(1.dp.toPx()))
                    if (isUnavailable) {
                        drawLine(
                            color = colors.onSurface,
                            start = Offset(size.width * 0.85f, size.height * 0.15f),
                            end = Offset(size.width * 0.15f, size.height * 0.85f),
                            strokeWidth = 3.dp.toPx(),
                        )
                    }
                }
                if (isSelected) {
                    Box(Modifier.size(48.dp).border(3.dp, colors.onSurface, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun BackgroundPreview(
    custom: CustomDiceColors,
    scheme: ColorScheme,
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .height(64.dp)
            .background(scheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        NumberDie(
            die = StandardDie.D6,
            value = 5,
            style = dieStyle(DiceColorMode.Custom, custom, scheme.surface.toArgb(), scheme.onSurface.toArgb()),
            size = 40.dp,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurface)
    }
}

@Composable
private fun ColorWarnings(custom: CustomDiceColors, light: ColorScheme, dark: ColorScheme) {
    val onLight = checkDiceColors(custom, light.surface.toArgb())
    val onDark = checkDiceColors(custom, dark.surface.toArgb())
    val warnings = buildList {
        if (onLight.facePipsLow) add(stringResource(R.string.warning_face_pips, "%.1f".format(onLight.facePipsRatio)))
        when {
            onLight.hardToSeeOnBackground && onDark.hardToSeeOnBackground -> add(
                stringResource(R.string.warning_hard_to_see_both),
            )

            onLight.hardToSeeOnBackground -> add(stringResource(R.string.warning_hard_to_see_light))

            onDark.hardToSeeOnBackground -> add(stringResource(R.string.warning_hard_to_see_dark))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        warnings.forEach { Warning(it) }
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
