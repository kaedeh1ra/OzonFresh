package com.example.app.data

import com.example.app.data.repository.CounterRepositoryImpl
import com.example.app.testing.FakeKeyValueStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class CounterRepositoryImplTest {

    @Test
    fun restoresSavedValue() {
        val storage = FakeKeyValueStorage()
        CounterRepositoryImpl(storage).set(7)
        assertEquals(7, CounterRepositoryImpl(storage).count.value)
    }

    @Test
    fun startsFromZeroWhenNothingSaved() {
        assertEquals(0, CounterRepositoryImpl(FakeKeyValueStorage()).count.value)
    }
}
