package com.example.echodrop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.PurgeExpiredUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val observeInbox: ObserveInboxUseCase,
    private val observeTransfers: ObserveTransfersUseCase,
    private val startTransfer: StartTransferUseCase,
    private val purgeExpiredUseCase: PurgeExpiredUseCase // Füge das UseCase als Dependency hinzu

) : ViewModel() {

    init {
        // Starte das Purge beim Initialisieren des ViewModels
        viewModelScope.launch {
            purgeExpiredUseCase(System.currentTimeMillis())
        }
    }

    val paketList: StateFlow<List<PaketUi>> = observeInbox()
    .map{list->list.map{it.toUi()}}
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transferLogs: StateFlow<List<TransferLogUi>> = observeTransfers()
    .map{list->list.map{it.toUi()}}
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun onShareClicked(paketId: PaketId, peerId: PeerId){
        viewModelScope.launch{
            startTransfer(paketId, peerId)
        }
    }
}