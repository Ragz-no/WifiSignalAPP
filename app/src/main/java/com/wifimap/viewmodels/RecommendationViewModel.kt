package com.wifimap.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.wifimap.models.RouterPlacement
import com.wifimap.models.SignalSample
import com.wifimap.modeling.PropagationModel
import com.wifimap.recommendations.RouterRecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing router placement recommendations.
 */
class RecommendationViewModel(application: Application) : AndroidViewModel(application) {

    private val propagationModel = PropagationModel()
    private val recommendationEngine = RouterRecommendationEngine(propagationModel)

    private val _recommendations = MutableLiveData<List<RouterPlacement>>()
    val recommendations: LiveData<List<RouterPlacement>> = _recommendations

    private val _isGenerating = MutableLiveData(false)
    val isGenerating: LiveData<Boolean> = _isGenerating

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    /**
     * Generates router placement recommendations from samples.
     */
    fun generateRecommendations(
        samples: List<SignalSample>,
        floorplanWidth: Double,
        floorplanHeight: Double,
        routerHeight: Double = 1.5
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                _isGenerating.postValue(true)
                val placements = recommendationEngine.generateRecommendations(
                    samples = samples,
                    floorplanWidth = floorplanWidth,
                    floorplanHeight = floorplanHeight,
                    routerHeight = routerHeight
                )
                _recommendations.postValue(placements)
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to generate recommendations: ${e.message}")
            } finally {
                _isGenerating.postValue(false)
            }
        }
    }

    /**
     * Gets the top 3 recommendations if available.
     */
    fun getTopRecommendations(): List<RouterPlacement> {
        return _recommendations.value?.take(3) ?: emptyList()
    }
}
