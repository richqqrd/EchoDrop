package com.example.echodrop.model.dataLayer.transport

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.impl.transport.MaintenanceSchedulerImpl
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaintenanceSchedulerImplTest {

    @Test
    fun startAndStop_doNotThrow() = runTest {
        val connRepo = mockk<ConnectionAttemptRepository>(relaxed = true)
        val peerRepo = mockk<PeerRepository>(relaxed = true)

        val scheduler = MaintenanceSchedulerImpl(connRepo, peerRepo)

   
        scheduler.start()
        scheduler.stop()
    }
} 