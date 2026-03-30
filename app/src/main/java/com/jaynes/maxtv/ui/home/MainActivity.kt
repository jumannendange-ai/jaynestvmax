package com.jaynes.maxtv.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jaynes.maxtv.R
import com.jaynes.maxtv.databinding.ActivityMainBinding
import com.jaynes.maxtv.ui.epg.EpgFragment
import com.jaynes.maxtv.ui.favorites.FavoritesFragment
import com.jaynes.maxtv.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> loadFragment(HomeFragment())
                R.id.nav_channels  -> loadFragment(ChannelsFragment())
                R.id.nav_epg       -> loadFragment(EpgFragment())
                R.id.nav_favorites -> loadFragment(FavoritesFragment())
                R.id.nav_profile   -> loadFragment(ProfileFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
        return true
    }
}
