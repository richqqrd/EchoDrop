@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.PurgeExpiredUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi

class InboxViewModelTest {

    private lateinit var vm: InboxViewModel
    private val observeInbox: ObserveInboxUseCase = mockk()
    private val observeTransfers: ObserveTransfersUseCase = mockk()
    private val startTransfer: StartTransferUseCase = mockk(relaxed = true)
    private val purgeExpired: PurgeExpiredUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { observeInbox.invoke() } returns flowOf(emptyList())
        every { observeTransfers.invoke() } returns flowOf(emptyList())

        vm = InboxViewModel(observeInbox, observeTransfers, startTransfer, purgeExpired)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() = runTest {
        val list = vm.paketList.value
        assertEquals(0, list.size)
    }
} 