package io.github.jamesmyatt.randomatizer.ui

import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jamesmyatt.randomatizer.R
import io.github.jamesmyatt.randomatizer.color.DieStyle
import io.github.jamesmyatt.randomatizer.color.dieStyle
import io.github.jamesmyatt.randomatizer.dice.DiceLimits
import io.github.jamesmyatt.randomatizer.dice.DieResult
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.history.HistoryEntry
import io.github.jamesmyatt.randomatizer.history.describe
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.Mode
import io.github.jamesmyatt.randomatizer.ui.theme.ApplyThemeMode
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val ROLL_FRAMES = 7
private const val ROLL_FRAME_MS = 40L

@Composable
fun RollerRoute(viewModel: RollerViewModel = viewModel(factory = RollerViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ApplyThemeMode(state?.settings?.themeMode)
    state?.let {
        RollerScreen(
            state = it,
            onRoll = viewModel::roll,
            onClearHistory = viewModel::clearHistory,
            onUpdateSettings = viewModel::updateSettings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RollerScreen(
    state: RollerUiState,
    onRoll: () -> Unit,
    onClearHistory: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val settings = state.settings
    val style = dieStyle(settings.diceFace, settings.customFace, colors.surface.toArgb(), colors.onSurface.toArgb())
    val animated = animatedResults(state.current)
    // Hide the newest history row until its roll animation has finished.
    val history = if (animated.animating) state.history.filter { it.id != state.current?.id } else state.history

    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { SelectionPanel(settings, onUpdateSettings) }
            item { DiceArea(state.current?.mode, animated.results, style) }
            if (settings.showTotal) {
                item { Total(animated.results) }
            }
            item { RollButton(settings, onRoll) }
            if (history.isNotEmpty()) {
                item { HistoryHeader(onClearHistory) }
                itemsIndexed(history, key = { _, entry -> entry.id }) { index, entry ->
                    HistoryRow(entry, isLatest = index == 0, showTotal = settings.showTotal)
                }
            }
        }
    }

    if (showSettings) {
        SettingsSheet(settings = settings, onUpdate = onUpdateSettings, onDismiss = { showSettings = false })
    }
}

private data class AnimatedResults(val results: List<DieResult>?, val animating: Boolean)

/** Shows random faces briefly before the real result. Cosmetic only: results come from the ViewModel. */
@Composable
private fun animatedResults(entry: HistoryEntry?): AnimatedResults {
    val animationsEnabled = animationsEnabled()
    // Saveable so a configuration change does not replay the animation.
    var lastAnimatedId by rememberSaveable { mutableLongStateOf(entry?.id ?: -1L) }
    // Decided during composition, not in the effect, so the first frame of a new roll is already animating.
    val animating = entry != null && animationsEnabled && entry.id != lastAnimatedId
    var faces by remember(entry?.id) { mutableStateOf(entry?.let(::randomFaces)) }
    LaunchedEffect(entry?.id) {
        if (entry != null && animating) {
            repeat(ROLL_FRAMES) {
                faces = randomFaces(entry)
                delay(ROLL_FRAME_MS)
            }
        }
        lastAnimatedId = entry?.id ?: -1L
    }
    return if (animating) AnimatedResults(faces, animating = true) else AnimatedResults(entry?.roll?.results, false)
}

private fun randomFaces(entry: HistoryEntry): List<DieResult> =
    entry.roll.results.map { DieResult(it.die, Random.nextInt(1, it.die.sides + 1)) }

@Composable
private fun animationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f }
}

@Composable
private fun SelectionPanel(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    val expanded = settings.selectionExpanded
    val summary = when (settings.mode) {
        Mode.Basic -> settings.basicCount.toString()
        Mode.Advanced -> settings.advancedSelection.summary()
    }
    val stateText = stringResource(if (expanded) R.string.expanded else R.string.collapsed)
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.animateContentSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { onUpdate { it.copy(selectionExpanded = !it.selectionExpanded) } }
                    .semantics { stateDescription = stateText }
                    .padding(start = 16.dp, end = 12.dp),
            ) {
                Text(stringResource(R.string.dice), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painterResource(if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more),
                    contentDescription = null,
                )
            }
            if (expanded) {
                when (settings.mode) {
                    Mode.Basic -> BasicSelector(settings, onUpdate)
                    Mode.Advanced -> AdvancedSelector(settings, onUpdate)
                }
            }
        }
    }
}

@Composable
private fun BasicSelector(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        CountButton(
            iconRes = R.drawable.ic_remove,
            description = stringResource(R.string.fewer_dice),
            enabled = settings.basicCount > DiceLimits.BASIC_MIN,
            size = 48.dp,
        ) { onUpdate { it.copy(basicCount = (it.basicCount - 1).coerceAtLeast(DiceLimits.BASIC_MIN)) } }
        Text(
            text = settings.basicCount.toString(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp),
        )
        CountButton(
            iconRes = R.drawable.ic_add,
            description = stringResource(R.string.more_dice),
            enabled = settings.basicCount < DiceLimits.BASIC_MAX,
            size = 48.dp,
        ) { onUpdate { it.copy(basicCount = (it.basicCount + 1).coerceAtMost(DiceLimits.BASIC_MAX)) } }
    }
}

@Composable
private fun AdvancedSelector(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
        val columns = if (maxWidth >= 320.dp) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StandardDie.entries.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { die -> DieCounter(die, settings, onUpdate, Modifier.weight(1f)) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DieCounter(
    die: StandardDie,
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = settings.advancedSelection
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.heightIn(min = 44.dp),
    ) {
        Text(die.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(44.dp))
        CountButton(
            iconRes = R.drawable.ic_remove,
            description = stringResource(R.string.remove_die, die.label),
            enabled = selection.canDecrement(die),
            size = 40.dp,
        ) {
            onUpdate { s ->
                val current = s.advancedSelection
                val count = current.count(die) - 1
                if (current.canDecrement(die)) s.copy(advancedSelection = current.withCount(die, count)) else s
            }
        }
        Text(
            text = selection.count(die).toString(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp),
        )
        CountButton(
            iconRes = R.drawable.ic_add,
            description = stringResource(R.string.add_die, die.label),
            enabled = selection.canIncrement(die),
            size = 40.dp,
        ) {
            onUpdate { s ->
                val current = s.advancedSelection
                val count = current.count(die) + 1
                if (current.canIncrement(die)) s.copy(advancedSelection = current.withCount(die, count)) else s
            }
        }
    }
}

@Composable
private fun CountButton(iconRes: Int, description: String, enabled: Boolean, size: Dp, onClick: () -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.5.dp, if (enabled) outline else outline.copy(alpha = 0.38f)),
        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.size(size),
    ) {
        Icon(painterResource(iconRes), contentDescription = description)
    }
}

@Composable
private fun DiceArea(mode: Mode?, results: List<DieResult>?, style: DieStyle) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
        if (results == null) {
            Text(
                text = stringResource(R.string.ready_to_roll),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
            )
            return@BoxWithConstraints
        }
        val size = dieSize(maxWidth, results.size)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(DIE_SPACING, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(DIE_SPACING),
            itemVerticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            results.forEach { result ->
                if (mode == Mode.Basic) {
                    PipDie(result.value, style, size)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        NumberDie(result.die, result.value, style, size)
                        Text(
                            text = result.die.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Total(results: List<DieResult>?) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.total),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            text = results?.sumOf { it.value }?.toString() ?: "–",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .alignByBaseline()
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun RollButton(settings: AppSettings, onRoll: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val label = when (settings.mode) {
        Mode.Basic -> stringResource(R.string.roll)
        Mode.Advanced -> settings.advancedSelection.total.let { pluralStringResource(R.plurals.roll_dice, it, it) }
    }
    Button(
        onClick = onRoll,
        colors = ButtonDefaults.buttonColors(containerColor = colors.onSurface, contentColor = colors.surface),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp).height(56.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun HistoryHeader(onClear: () -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onClear,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, isLatest: Boolean, showTotal: Boolean) {
    val color = if (isLatest) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
    ) {
        Text(
            entry.describe(),
            color = color,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (showTotal) {
            Text(
                entry.roll.total.toString(),
                color = color,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
