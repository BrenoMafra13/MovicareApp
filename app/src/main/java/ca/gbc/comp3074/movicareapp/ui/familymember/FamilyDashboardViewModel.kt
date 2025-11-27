package ca.gbc.comp3074.movicareapp.ui.familymember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyDashboardViewModel(
    private val db: AppDatabase,
    private val userId: Long
) : ViewModel() {

    private val userDao = db.userDao()
    private val relationshipDao = db.userRelationshipDao()

    // 1. Get all accepted relationships and fetch User entities
    val linkedSeniors: StateFlow<List<UserEntity>> = relationshipDao.getAllAcceptedRelationships(userId)
        .map { relationships ->
            relationships.map { rel ->
                if (rel.requesterId == userId) rel.targetId else rel.requesterId
            }
        }
        .flatMapLatest { ids ->
            flow {
                val users = ids.mapNotNull { id -> userDao.getById(id) }
                emit(users)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Pending Invitations Count (for Badge)
    val hasPendingInvitations: StateFlow<Boolean> = relationshipDao.getPendingRequests(userId)
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // 3. Selected Senior State
    private val _selectedSeniorId = MutableStateFlow<Long?>(null)
    val selectedSeniorId: StateFlow<Long?> = _selectedSeniorId

    fun selectSenior(id: Long) {
        _selectedSeniorId.value = id
    }
    
    // Auto-select first senior if none selected
    init {
        viewModelScope.launch {
            linkedSeniors.collect { seniors ->
                if (_selectedSeniorId.value == null && seniors.isNotEmpty()) {
                    _selectedSeniorId.value = seniors.first().id
                }
            }
        }
    }
}
