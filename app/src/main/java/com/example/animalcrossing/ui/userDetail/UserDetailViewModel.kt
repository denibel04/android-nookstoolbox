package com.example.animalcrossing.ui.userDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.db.asVillager
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.data.repository.VillagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(private val repository: UserRepository, private val villagerRepository: VillagerRepository):ViewModel(){
    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState>
        get() = _uiState.asStateFlow()



    fun getUser(uid: String) {
        viewModelScope.launch {
            val user = repository.getUserDetail(uid)

            val villagers = user.villagers

            val villagerEntities = villagers.map { name ->
                async { villagerRepository.getVillager(name).first() }
            }.awaitAll()

            val villagerList = villagerEntities.asVillager()

            val filledVillagerList = MutableList(10) { index ->
                villagerList.getOrNull(index)
            }

            _uiState.value = _uiState.value.copy(
                uid = user.uid,
                email = user.email,
                username = user.username,
                profile_picture = user.profile_picture,
                dreamCode = user.dreamCode,
                followers = user.followers,
                following = user.following,
                islandName = user.islandName,
                hemisphere = user.hemisphere,
                islandExists = user.islandExists,
                villagers = filledVillagerList,
            )
            }
        }
    }

