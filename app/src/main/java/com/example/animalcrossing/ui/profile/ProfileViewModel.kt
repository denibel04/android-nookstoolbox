package com.example.animalcrossing.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.data.repository.VillagerRepository
import com.example.animalcrossing.ui.list.VillagerListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()


    init {

        viewModelScope.launch {
            repository.profile.collect { user ->
                Log.d("ENTRA AQUI", user.toString())
                _uiState.value = _uiState.value.copy(currentUser = flowOf(user))
            }
        }

        viewModelScope.launch {
            val friends = repository.getFriends()
            _uiState.value = _uiState.value.copy(friends = friends)
        }

    }

    suspend fun changeUsername(newUsername: User) {
        repository.changeUsername(newUsername)
    }

    suspend fun changeDreamCode(newDreamCode: User) {
        repository.changeDreamCode(newDreamCode)
    }

}