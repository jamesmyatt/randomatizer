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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import io.github.jamesmyatt.randomatizer.colour.checkDiceColours
import io.github.jamesmyatt.randomatizer.colour.dieStyle
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.CustomDiceColours
import io.github.jamesmyatt.randomatizer.settings.DiceColourMode
import io.github.jamesmyatt.randomatizer.settings.Mode

private enum class ColourTarget { Face, Pips }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        ) {
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)
            ModeSection(settings.mode) { mode -> onUpdate { it.copy(mode = mode) } }
            DiceColourSection(settings) { mode -> onUpdate { it.copy(diceColourMode = mode) } }
            if (settings.diceColourMode == DiceColourMode.Custom) {
                CustomColoursEditor(
                    custom = settings.customColours,
                    onPickFace = { argb ->
                        onUpdate { s -> if (argb == s.customColours.pips) s else s.copy(customColours = s.customColours.copy(face = argb)) }
                    },
                    onPickPips = { argb ->
                        onUpdate { s -> if (argb == s.customColours.face) s else s.copy(customColours = s.customColours.copy(pips = argb)) }
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
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSection(selected: Mode, onSelect: (Mode) -> Unit) {
    val colours = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.mode))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Mode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, Mode.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colours.onSurface,
                        activeContentColor = colours.surface,
                    ),
                    icon = {},
                ) {
                    Text(stringResource(if (mode == Mode.Basic) R.string.mode_basic else R.string.mode_advanced))
                }
            }
        }
    }
}

@Composable
private fun DiceColourSection(settings: AppSettings, onSelect: (DiceColourMode) -> Unit) {
    val colours = MaterialTheme.colorScheme
    Column(Modifier.selectableGroup()) {
        SectionTitle(stringResource(R.string.dice_colours))
        DiceColourMode.entries.forEach { mode ->
            val selected = mode == settings.diceColourMode
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
                    colors = RadioButtonDefaults.colors(selectedColor = colours.onSurface),
                )
                Text(
                    text = stringResource(
                        when (mode) {
                            DiceColourMode.System -> R.string.dice_colours_system
                            DiceColourMode.SystemInverted -> R.string.dice_colours_system_inverted
                            DiceColourMode.Custom -> R.string.dice_colours_custom
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 14.dp).weight(1f),
                )
                NumberDie(
                    die = StandardDie.D6,
                    value = 5,
                    style = dieStyle(mode, settings.customColours, colours.surface.toArgb(), colours.onSurface.toArgb()),
                    size = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun CustomColoursEditor(custom: CustomDiceColours, onPickFace: (Int) -> Unit, onPickPips: (Int) -> Unit) {
    var open by rememberSaveable { mutableStateOf<ColourTarget?>(null) }
    val context = LocalContext.current
    val light = remember(context) { dynamicLightColorScheme(context) }
    val dark = remember(context) { dynamicDarkColorScheme(context) }

    Column {
        ColourRow(stringResource(R.string.face), custom.face) { open = if (open == ColourTarget.Face) null else ColourTarget.Face }
        if (open == ColourTarget.Face) {
            SwatchGrid(selected = custom.face, unavailable = custom.pips, unavailableLabel = R.string.swatch_same_as_pips, onPick = onPickFace)
        }
        ColourRow(stringResource(R.string.pips), custom.pips) { open = if (open == ColourTarget.Pips) null else ColourTarget.Pips }
        if (open == ColourTarget.Pips) {
            SwatchGrid(selected = custom.pips, unavailable = custom.face, unavailableLabel = R.string.swatch_same_as_face, onPick = onPickPips)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 10.dp)) {
            BackgroundPreview(custom, light, stringResource(R.string.light), Modifier.weight(1f))
            BackgroundPreview(custom, dark, stringResource(R.string.dark), Modifier.weight(1f))
        }
        ColourWarnings(custom, light, dark)
    }
}

@Composable
private fun ColourRow(label: String, argb: Int, onClick: () -> Unit) {
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
            text = colourName(argb),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun colourName(argb: Int): String =
    DicePalette.firstOrNull { it.argb == argb }?.let { stringResource(it.name) }
        ?: "#%06X".format(argb and 0xFFFFFF)

@Composable
private fun SwatchGrid(selected: Int, unavailable: Int, unavailableLabel: Int, onPick: (Int) -> Unit) {
    val colours = MaterialTheme.colorScheme
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
                    drawCircle(colours.outline, style = Stroke(1.dp.toPx()))
                    if (isUnavailable) {
                        drawLine(
                            color = colours.onSurface,
                            start = Offset(size.width * 0.85f, size.height * 0.15f),
                            end = Offset(size.width * 0.15f, size.height * 0.85f),
                            strokeWidth = 3.dp.toPx(),
                        )
                    }
                }
                if (isSelected) {
                    Box(Modifier.size(48.dp).border(3.dp, colours.onSurface, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun BackgroundPreview(custom: CustomDiceColours, scheme: ColorScheme, label: String, modifier: Modifier = Modifier) {
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
            style = dieStyle(DiceColourMode.Custom, custom, scheme.surface.toArgb(), scheme.onSurface.toArgb()),
            size = 40.dp,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurface)
    }
}

@Composable
private fun ColourWarnings(custom: CustomDiceColours, light: ColorScheme, dark: ColorScheme) {
    val onLight = checkDiceColours(custom, light.surface.toArgb())
    val onDark = checkDiceColours(custom, dark.surface.toArgb())
    val warnings = buildList {
        if (onLight.facePipsLow) add(stringResource(R.string.warning_face_pips, "%.1f".format(onLight.facePipsRatio)))
        when {
            onLight.hardToSeeOnBackground && onDark.hardToSeeOnBackground -> add(stringResource(R.string.warning_hard_to_see_both))
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
    val colours = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(colours.errorContainer, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = colours.onErrorContainer)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colours.onErrorContainer)
    }
}
