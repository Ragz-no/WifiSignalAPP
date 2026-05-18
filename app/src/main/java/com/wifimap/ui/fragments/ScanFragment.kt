package com.wifimap.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wifimap.R
import com.wifimap.viewmodels.ScanViewModel

/**
 * Fragment for guided WiFi scanning with RSSI, band, and throughput sampling.
 */
class ScanFragment : Fragment() {

    private lateinit var scanViewModel: ScanViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanViewModel = ViewModelProvider(this).get(ScanViewModel::class.java)

        // Setup scan UI and listeners
        observeScanData()
    }

    private fun observeScanData() {
        scanViewModel.samples.observe(viewLifecycleOwner) { samples ->
            // Update UI with samples
        }

        scanViewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            // Update UI loading state
        }

        scanViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // Display error message
        }
    }
}
