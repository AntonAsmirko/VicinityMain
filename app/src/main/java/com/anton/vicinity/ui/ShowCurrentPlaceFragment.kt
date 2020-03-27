package com.anton.vicinity.ui


import android.annotation.SuppressLint
import android.location.Address
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.anton.vicinity.R
import kotlinx.android.synthetic.main.fragment_show_current_place.*

/**
 * A simple [Fragment] subclass.
 */
class ShowCurrentPlaceFragment : Fragment() {
    private lateinit var fragmentView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_show_current_place, container, false)
        return fragmentView
    }

    @SuppressLint("SetTextI18n")
    fun setPlace(address: List<Address>) {
        location.text = address[0].thoroughfare?: ""  + " " + address[0].getAddressLine(0)?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        MapFragmentViewModel.isTopBarEmpty = true
        MapFragmentViewModel.topBarState = MapFragmentViewModel.TopBarState.EMPTY
    }
}
