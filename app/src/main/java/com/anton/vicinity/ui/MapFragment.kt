package com.anton.vicinity.ui


import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anton.vicinity.R
import com.anton.vicinity.adapters.EventCreationInviteFriendsAdapter
import com.anton.vicinity.data.Event
import com.anton.vicinity.data.MapRepository
import com.anton.vicinity.data.User
import com.anton.vicinity.data.UserRepository
import com.anton.vicinity.ui.MapFragmentViewModel.cameraPosition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.fragment_map.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {

    private lateinit var fragmentView: View
    private lateinit var googleMap: GoogleMap
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private lateinit var createEventMarkerButton: ImageButton
    private lateinit var searchForPlaceButton: ImageButton
    private lateinit var showCurrentPlaceFragment: ShowCurrentPlaceFragment
    private lateinit var geocodder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var bottomNavigationOwner: BottomNavigationVisibilityChangeManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false)
        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(activity as Activity)
        MapFragmentViewModel.nearestEvents.observe(this, Observer { events ->
            if (::googleMap.isInitialized)
                events.forEach {
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.eventLat ?: 0.0, it.eventLng ?: 0.0))
                    )
                }
        })
        MapFragmentViewModel.isTopBarEmpty = true
        mBottomSheetBehavior =
            BottomSheetBehavior.from(fragmentView.findViewById(R.id.bottom_sheet))
        mBottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomNavigationOwner.onBottomNavigationStateChange(View.VISIBLE)
                }
            }

        })
        val recycler: RecyclerView = fragmentView.findViewById(R.id.recyclerView)
        val linearLayout = LinearLayoutManager(activity)
        recycler.layoutManager = linearLayout
        val eventCreationInviteFriendsAdapter =
            EventCreationInviteFriendsAdapter(context!!)
        UserRepository.usersFriends.observe(this, Observer {
            eventCreationInviteFriendsAdapter.update(it)
        })
        recycler.adapter = eventCreationInviteFriendsAdapter
        geocodder = Geocoder(context, Locale.getDefault())
        createEventMarkerButton = fragmentView.findViewById(R.id.createEventButton)
        createEventMarkerButton.setOnClickListener { onCreateEventButtonPressed() }
        UserRepository.loadFriends()
        return fragmentView
    }
    override fun onStart() {
        super.onStart()
        searchForPlaceButton = fragmentView.findViewById(R.id.searchPlaceButton)
        searchForPlaceButton.setOnClickListener { searchButtonClickListener() }
        showCurrentPlaceFragment = ShowCurrentPlaceFragment()
        if (MapFragmentViewModel.isTopBarEmpty) {
            fragmentManager!!.beginTransaction()
                .add(R.id.topBarPlaceholder, showCurrentPlaceFragment)
                .commit()
            MapFragmentViewModel.isTopBarEmpty = false
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetBehavior.peekHeight = 1000

        map?.let {
            googleMap = it
            it.isMyLocationEnabled = true
            if (MapFragmentViewModel.isCameraPositionInitialized()) {
                moveToCurrentPositionAndLoadEvents(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude
                )
            } else {
                fusedLocationClient
                    .lastLocation.addOnSuccessListener { location ->
                    if (location != null)
                        moveToCurrentPositionAndLoadEvents(location.latitude, location.longitude)
                }
            }
            if (MapFragmentViewModel.isCameraPositionInitialized())
                it.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            it.setOnMarkerClickListener(this)
            it.setOnMapClickListener {
                if (MapFragmentViewModel.topBarState == MapFragmentViewModel.TopBarState.SEARCH
                    || MapFragmentViewModel.topBarState == MapFragmentViewModel.TopBarState.EMPTY
                ) {
                    fragmentManager!!.beginTransaction()
                        .replace(R.id.topBarPlaceholder, ShowCurrentPlaceFragment())
                        .commit()
                    MapFragmentViewModel.topBarState = MapFragmentViewModel.TopBarState.SHOW_PLACE
                }
                val center: CameraPosition = googleMap.cameraPosition
                var address: List<Address> =
                    geocodder.getFromLocation(center.target.latitude,
                        center.target.longitude, 1)
                if (!MapFragmentViewModel.isCameraPositionInitialized()
                    || cameraPosition != center
                ) {
                    // this fragment is not in placeholder
                    showCurrentPlaceFragment.setPlace(address)
                    cameraPosition = center
                } else {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    bottomNavigationOwner.onBottomNavigationStateChange(View.GONE)
                }
                MapFragmentViewModel.nearestEvents.observe(
                    this,
                    androidx.lifecycle.Observer { events ->
                        events.forEach { event ->
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(event.eventLat ?: 0.0, event.eventLng ?: 0.0))
                            )
                        }
                    })
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val eventDescriptionFragment = EventDescriptionFragment()
        val fragmentManager = activity!!.supportFragmentManager
        cameraPosition = googleMap.cameraPosition
        fragmentManager.beginTransaction()
            .replace(R.id.map_placeholder, eventDescriptionFragment)
            .addToBackStack("EventDescription")
            .commit()
        println("MarkerClicked")
        MapFragmentViewModel.isTopBarEmpty = true
        return true
    }

    override fun onMarkerDrag(p0: Marker?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMarkerDragStart(p0: Marker?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMarkerDragEnd(p0: Marker?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        map_view.onCreate(savedInstanceState)
        map_view.onResume()

        map_view.getMapAsync(this)

    }

    fun setBottomNavigationOwner(bottomNavigationOwner:
                                 BottomNavigationVisibilityChangeManager){
        this.bottomNavigationOwner = bottomNavigationOwner
    }

    private fun moveToCurrentPositionAndLoadEvents(lat: Double, lng: Double) {
        googleMap.moveCamera(
            CameraUpdateFactory
                .newLatLngZoom(LatLng(lat, lng), 15.0f)
        )
        MapRepository.loadNearestEvents(LatLng(lat, lng))
    }

    private fun onCreateEventButtonPressed() {
        val dravables = listOf(
            R.drawable.baseline_chat_white_36dp,
            R.drawable.baseline_done_black_36dp, R.drawable.round_zoom_in_black_24dp,
            R.drawable.send_button, R.drawable.twotone_games_black_18dp
        )
        val center: LatLng = googleMap.cameraPosition.target
        val random = Random(142656467L)
        googleMap.addMarker(
            MarkerOptions()
                .position(center).icon(BitmapDescriptorFactory.fromResource(dravables[random.nextInt() % dravables.size]))
        )
        MapFragmentViewModel.saveEvent(
            Event(
                "KEK", "KEK",
                "KEK", center.latitude, center.longitude, mutableListOf<String>(),
                "KEK", "KEK"
            )
        )
    }

    private fun searchButtonClickListener() {
        if (MapFragmentViewModel.topBarState == MapFragmentViewModel.TopBarState.SHOW_PLACE
            || MapFragmentViewModel.topBarState == MapFragmentViewModel.TopBarState.EMPTY
        ) {
            fragmentManager!!.beginTransaction()
                .replace(R.id.topBarPlaceholder, SearchPlaceFragment())
                .commit()
            MapFragmentViewModel.topBarState = MapFragmentViewModel.TopBarState.SEARCH
        } else {
            //search
        }
    }

    interface BottomNavigationVisibilityChangeManager{
        fun onBottomNavigationStateChange(visibility: Int)
    }
}
