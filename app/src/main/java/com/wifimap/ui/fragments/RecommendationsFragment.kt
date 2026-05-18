package com.wifimap.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wifimap.R
import com.wifimap.viewmodels.RecommendationViewModel

/**
 * Fragment for displaying top 3 router placement recommendations with expected improvements.
 */
class RecommendationsFragment : Fragment() {

    private lateinit var recommendationViewModel: RecommendationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recommendationViewModel = ViewModelProvider(this).get(RecommendationViewModel::class.java)

        // Setup recommendations display
        observeRecommendations()
    }

    private fun observeRecommendations() {
        recommendationViewModel.recommendations.observe(viewLifecycleOwner) { placements ->
            // Display top 3 placements with details
        }

        recommendationViewModel.isGenerating.observe(viewLifecycleOwner) { isGenerating ->
            // Update UI loading state
        }

        recommendationViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // Display error message
        }
    }
}
