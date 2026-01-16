package com.example.obstacles.highscores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.obstacles.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class HighScoresMapFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: HighScoresViewModel by activityViewModels {
        HighScoresViewModelFactory(HighScoresServiceLocator.repository(requireContext()))
    }

    private var googleMap: GoogleMap? = null
    private val markers = mutableMapOf<HighScoreEntry, Marker>()
    private var pendingScores: List<HighScoreEntry> = emptyList()
    private var pendingSelection: HighScoreEntry? = null
    private var isMapLoaded = false
    private var lastBounds: LatLngBounds? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_high_scores_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val locationText = view.findViewById<TextView>(R.id.mapLocationLabel)
        val scoreText = view.findViewById<TextView>(R.id.mapScoreLabel)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
                ?: return

        mapFragment.getMapAsync(this)

        viewModel.topScores.observe(viewLifecycleOwner) { scores ->
            pendingScores = scores
            renderMarkers(scores)
        }

        viewModel.selectedScore.observe(viewLifecycleOwner) { entry ->
            pendingSelection = entry
            if (entry == null) {
                locationText.text = getString(R.string.high_scores_placeholder_location)
                scoreText.text = getString(R.string.high_scores_placeholder_score)
                return@observe
            }
            locationText.text = entry.location.label
            scoreText.text = getString(
                R.string.high_score_value_format,
                entry.distance,
                entry.coins
            )
            focusOnSelection(entry)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMapLoadedCallback {
            isMapLoaded = true
            renderMarkers(pendingScores)
            pendingSelection?.let { focusOnSelection(it) }
        }
        map.setOnMarkerClickListener { marker ->
            val entry = marker.tag as? HighScoreEntry
            if (entry != null) {
                viewModel.select(entry)
                true
            } else {
                false
            }
        }
        renderMarkers(pendingScores)
        pendingSelection?.let { focusOnSelection(it) }
    }

    private fun renderMarkers(scores: List<HighScoreEntry>) {
        val map = googleMap ?: return
        map.clear()
        markers.clear()
        lastBounds = null
        if (scores.isEmpty() || !isMapLoaded) return

        val boundsBuilder = LatLngBounds.Builder()
        scores.forEach { entry ->
            val position = LatLng(entry.location.latitude, entry.location.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(entry.location.label)
                    .snippet(
                        getString(
                            R.string.high_score_value_format,
                            entry.distance,
                            entry.coins
                        )
                    )
            )
            if (marker != null) {
                marker.tag = entry
                markers[entry] = marker
            }
            boundsBuilder.include(position)
        }
        lastBounds = boundsBuilder.build()
        lastBounds?.let { bounds ->
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
        }
    }

    private fun focusOnSelection(entry: HighScoreEntry) {
        val map = googleMap ?: return
        val marker = markers[entry]
        val position = LatLng(entry.location.latitude, entry.location.longitude)
        if (!isMapLoaded) return
        val bounds = lastBounds
        val shouldFitAll = bounds != null && markers.size > 1
        if (shouldFitAll) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING))
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
        }
        marker?.showInfoWindow()
    }

    companion object {
        private const val DEFAULT_ZOOM = 12f
        private const val MAP_PADDING = 120
    }
}
