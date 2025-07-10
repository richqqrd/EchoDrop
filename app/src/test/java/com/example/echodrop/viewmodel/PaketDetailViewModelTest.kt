@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.usecase.paket.*
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PaketDetailViewModelTest {

    private lateinit var vm: PaketDetailViewModel

    private val getPaketDetail: GetPaketDetailUseCase = mockk()
    private val getFilesForPaket: GetFilesForPaketUseCase = mockk()
    private val updatePaketMeta: UpdatePaketMetaUseCase = mockk(relaxed = true)
    private val deletePaket: DeletePaketUseCase = mockk(relaxed = true)
    private val startTransfer: StartTransferUseCase = mockk(relaxed = true)
    private val startBeaconing: StartBeaconingUseCase = mockk(relaxed = true)
    private val stopBeaconing: StopBeaconingUseCase = mockk(relaxed = true)
    private val observeDiscoveredDevices: ObserveDiscoveredDevicesUseCase = mockk()
    private val connectToDevice: ConnectToDeviceUseCase = mockk(relaxed = true)
    private val savePeer: SavePeerUseCase = mockk(relaxed = true)
    private val observeConnection: ObserveConnectionStateUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { observeDiscoveredDevices.invoke() } returns flowOf(emptyList())
        coEvery { observeConnection.invoke() } returns flowOf(ConnectionState(isConnected = false, connectedDevices = emptySet()))

        vm = PaketDetailViewModel(
            getPaketDetail,
            getFilesForPaket,
            updatePaketMeta,
            deletePaket,
            startTransfer,
            startBeaconing,
            stopBeaconing,
            observeDiscoveredDevices,
            connectToDevice,
            savePeer,
            observeConnection
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleEditMode flips flag`() {
        val before = vm.state.value.isEditing
        vm.toggleEditMode()
        assertEquals(!before, vm.state.value.isEditing)
    }
} 