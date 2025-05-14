package com.example.echodrop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.usecase.transfer.CancelTransferUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.PauseTransferUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ResumeTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val observeTransfers: ObserveTransfersUseCase,
    private val pause: PauseTransferUseCase,
    private val resume: ResumeTransferUseCase,
    private val cancel: CancelTransferUseCase
) : ViewModel() {

    val transfers: StateFlow<List<TransferLogUi>> = observeTransfers()
        .map { list -> list.map { it.toUi() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onPause(paketId: PaketId, peerId: PeerId) = viewModelScope.launch {
        pause(paketId, peerId)
    }

    fun onResume(paketId: PaketId, peerId: PeerId) = viewModelScope.launch {
        resume(paketId, peerId)
    }

    fun onCancel(paketId: PaketId, peerId: PeerId) = viewModelScope.launch {
        cancel(paketId, peerId)
    }
}
