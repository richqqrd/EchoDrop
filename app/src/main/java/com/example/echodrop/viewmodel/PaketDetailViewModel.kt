package com.example.echodrop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.DeletePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.UpdatePaketMetaUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaketDetailState(
    val paket: PaketUi? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false
)

@HiltViewModel
class PaketDetailViewModel @Inject constructor(
    private val getPaketDetail: GetPaketDetailUseCase,
    private val getFilesForPaket: GetFilesForPaketUseCase,
    private val updatePaketMeta: UpdatePaketMetaUseCase,
    private val deletePaket: DeletePaketUseCase,
    private val startTransfer: StartTransferUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PaketDetailState(isLoading = true))
    val state: StateFlow<PaketDetailState> = _state

    fun loadPaketDetail(paketId: String) {
        val id = PaketId(paketId)
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val paket = getPaketDetail(id)
                if (paket != null) {
                    // Lade die Dateien für das Paket
                    val files = getFilesForPaket(id)
                    
                    // Wandle das Paket in ein UI-Modell um und aktualisiere den State
                    _state.update { 
                        it.copy(
                            paket = paket.toDetailUi(files.map { file -> file.toUi() }),
                            isLoading = false
                        ) 
                    }
                } else {
                    _state.update { 
                        it.copy(
                            error = "Paket nicht gefunden",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Fehler beim Laden des Pakets: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun updatePaketSettings(ttlSeconds: Int, priority: Int) {
        val currentPaket = state.value.paket ?: return
        _state.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                // Rufe den UseCase auf, um die Paket-Metadaten zu aktualisieren
                updatePaketMeta(currentPaket.id, ttlSeconds, priority)
                
                // Lade das Paket neu, um die aktualisierten Werte zu zeigen
                loadPaketDetail(currentPaket.id.value)
                
                // Deaktiviere den Bearbeitungsmodus
                _state.update { it.copy(isEditing = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Fehler beim Aktualisieren des Pakets: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onDeletePaket() {
        val currentPaket = state.value.paket ?: return
        _state.update { it.copy(isDeleting = true) }
        
        viewModelScope.launch {
            try {
                // Rufe den UseCase auf, um das Paket zu löschen
                deletePaket(currentPaket.id)
                _state.update { it.copy(isDeleting = false) }
                // Nach dem Löschen wird in der UI zur vorherigen Ansicht zurücknavigiert
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Fehler beim Löschen des Pakets: ${e.message}",
                        isDeleting = false
                    ) 
                }
            }
        }
    }

    fun onSharePaket(peerId: PeerId) {
        val currentPaket = state.value.paket ?: return
        
        viewModelScope.launch {
            try {
                // Rufe den UseCase auf, um die Übertragung zu starten
                startTransfer(currentPaket.id, peerId)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Fehler beim Teilen des Pakets: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun toggleEditMode() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}