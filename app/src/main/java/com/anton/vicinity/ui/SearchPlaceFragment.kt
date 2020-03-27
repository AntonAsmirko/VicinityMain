package com.anton.vicinity.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.anton.vicinity.R

/**
 * A simple [Fragment] subclass.
 */
class SearchPlaceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        MapFragmentViewModel.isTopBarEmpty = true
        MapFragmentViewModel.topBarState = MapFragmentViewModel.TopBarState.EMPTY
    }


}
