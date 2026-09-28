package io.github.jamesmyatt.randomatizer.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.github.jamesmyatt.randomatizer.dice.DiceRoller
import io.github.jamesmyatt.randomatizer.dice.SeededRandomSource
import io.github.jamesmyatt.randomatizer.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RollerViewModelTest {
    private class InMemoryDataStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() =
        RollerViewModel(SettingsRepository(InMemoryDataStore()), DiceRoller(SeededRandomSource(1)))

    @Test
    fun `rolls are added to the history`() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.roll()
        vm.roll()
        assertEquals(2, vm.uiState.value?.history?.size)
    }

    @Test
    fun `turning history off discards it`() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.roll()
        vm.updateSettings { it.copy(historyEnabled = false) }
        assertTrue(vm.uiState.value!!.history.isEmpty())
        // Nothing was kept in memory while it was off.
        vm.updateSettings { it.copy(historyEnabled = true) }
        assertTrue(vm.uiState.value!!.history.isEmpty())
    }

    @Test
    fun `rolls are not added while history is off`() = runTest(dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        vm.updateSettings { it.copy(historyEnabled = false) }
        vm.roll()
        assertNotNull(vm.uiState.value!!.current)
        vm.updateSettings { it.copy(historyEnabled = true) }
        assertTrue(vm.uiState.value!!.history.isEmpty())
    }
}
