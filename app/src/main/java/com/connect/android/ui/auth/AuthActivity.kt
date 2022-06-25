package com.connect.android.ui.auth

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.NavInflater
import androidx.navigation.fragment.NavHostFragment
import com.connect.android.R
import com.connect.android.database.UserDatastore
import com.connect.android.databinding.ActivityAuthBinding
import com.connect.android.models.res.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var userDatastore: UserDatastore

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var graphInflater: NavInflater
    private lateinit var navGraph: NavGraph

    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {

        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // It will store main memory
        // Next time, when we access token it will quickly give me the result and it won't cause an ANR error
        userDatastore = UserDatastore(this)
        lifecycleScope.launch {
            userDatastore.isVerified.first()
        }

        // Setting theme mode
        setupThemeMode()

        // Keep the splash screen visible for this Activity
        splashScreen.setKeepOnScreenCondition {
            authViewModel.isLoading.value == true
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup nav graph
        initNavGraph()

        // Observing live data
        observingData()

        if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decorView = window.decorView
                val wic = WindowInsetsControllerCompat(window, decorView)
                wic.isAppearanceLightStatusBars = true
            }
        }
    }

    private fun initNavGraph() {
        // Programmatically inflating nav graph
        navHostFragment = auth_nav_host_fragment as NavHostFragment
        graphInflater = navHostFragment.navController.navInflater
        navGraph = graphInflater.inflate(R.navigation.auth_nav_graph)
    }

    private fun setupThemeMode() {
        userDatastore.theme.asLiveData().observe(this) {
            when (it) {
                AppCompatDelegate.MODE_NIGHT_NO -> {
                    // Light theme mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    // Dark theme mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                else -> {
                    // System default theme mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    private fun observingData() {
        /**
         * If user has verified their account but,
         * username and password haven't been saved to db yet, because
         * User would close the app after verified the email
         */
        authViewModel.navigateToUsernameFragment.observe(this) {
            if (it) {

                navGraph.setStartDestination(R.id.usernameFragment)
                navHostFragment.navController.graph = navGraph

                authViewModel.navigateToUsernameFragmentDone()
            }
        }

        authViewModel.navigateToWelcomePage.observe(this) {
            it?.let { user ->
                // Navigate to Welcome page
                sendUserToWelcomePage(user)
            }
        }
    }

    private fun sendUserToWelcomePage(user: User) {
        val bundle = Bundle()
        bundle.putParcelable("user", user)
        navGraph.setStartDestination(R.id.homeFragment2)
        navHostFragment.navController.setGraph(navGraph, bundle)
    }
}