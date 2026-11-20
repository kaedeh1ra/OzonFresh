package ru.kaed.ozonfresh.presentation.home

import ru.kaed.ozonfresh.domain.usecase.counter.IncrementCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ObserveCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ResetCounterUseCase
import ru.kaed.ozonfresh.presentation.home.HomeContract.Effect
import ru.kaed.ozonfresh.presentation.home.HomeContract.Intent
import ru.kaed.ozonfresh.testing.FakeCounterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = FakeCounterRepository(initial = 5)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HomeViewModel(
        ObserveCounterUseCase(repository),
        IncrementCounterUseCase(repository),
        ResetCounterUseCase(repository),
    )

    @Test
    fun startsFromRepositoryValue() {
        assertEquals(5, viewModel().state.value.count)
    }

    @Test
    fun incrementUpdatesState() {
        val vm = viewModel()
        vm.onIntent(Intent.Increment)
        assertEquals(6, vm.state.value.count)
        assertEquals(6, repository.count.value)
    }

    @Test
    fun resetClearsAndNotifies() = runTest {
        val vm = viewModel()
        vm.onIntent(Intent.Reset)
        assertEquals(0, vm.state.value.count)
        assertEquals(Effect.ShowMessage("Счётчик сброшен"), vm.effects.first())
    }
}
