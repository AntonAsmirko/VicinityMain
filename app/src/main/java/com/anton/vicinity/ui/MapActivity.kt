package com.anton.vicinity.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import android.content.Context
import android.widget.Toast
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import android.util.Log
import android.location.LocationManager
import android.content.Intent
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.anton.vicinity.Constants.*
import com.anton.vicinity.R
import androidx.fragment.app.Fragment
import com.anton.vicinity.Utils.BottomNavigationHelper
import com.google.android.material.bottomnavigation.BottomNavigationView


class MapActivity : AppCompatActivity(),
    MapFragment.BottomNavigationVisibilityChangeManager {

    private lateinit var fragmentManager: FragmentManager
    private val TAG = "MainActivity"
    private var mLocationPermissionGranted = false
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fragmentManager = supportFragmentManager

        if (checkMapServices()) {
            if (!mLocationPermissionGranted) {
                getLocationPermission()
            }
        }
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.item1
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item2 -> {
                    val userProfileFragment = UserProfileFragment()
                    bottomNavigationViewClickProcessor(userProfileFragment, 1)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.item1 -> {
                    val mapFragment = MapFragment()
                    bottomNavigationViewClickProcessor(mapFragment, 0)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        fragmentManager.addOnBackStackChangedListener {
            if (fragmentManager.backStackEntryCount < BottomNavigationHelper.backStackCount) {
                BottomNavigationHelper.backStackItems.removeAt(BottomNavigationHelper.backStackItems.size - 1)
                if (BottomNavigationHelper.backStackItems.size == 0) {
                    bottomNavigationView.menu.getItem(0).isChecked = true
                } else {
                    bottomNavigationView.menu.getItem(
                        BottomNavigationHelper
                            .backStackItems[BottomNavigationHelper.backStackItems.size - 1]
                    )
                        .isChecked = true
                }
            }
            BottomNavigationHelper.backStackCount = fragmentManager.backStackEntryCount
        }
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = MapFragment()
        fragmentTransaction.add(R.id.map_placeholder, fragment)
        fragmentTransaction.commit()
    }

    override fun onBottomNavigationStateChange(visibility: Int) {
        bottomNavigationView.visibility = visibility
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is MapFragment) {
            fragment.setBottomNavigationOwner(this)
        }
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun isServicesOK(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")

        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MapActivity)

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it")
            val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MapActivity, available, ERROR_DIALOG_REQUEST)
            dialog.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val gpsOptionsIntent = Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            startActivity(gpsOptionsIntent)
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                val enableGpsIntent =
                    Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            })
        val alert = builder.create()
        alert.show()
    }

    private fun bottomNavigationViewClickProcessor(fragment: Fragment, item: Int) {
        fragmentManager.beginTransaction()
            .replace(R.id.map_placeholder, fragment)
            .addToBackStack("")
            .commit()
        BottomNavigationHelper.backStackItems.add(item)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

}
