package com.connect.android.ui.home.like

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.connect.android.R
import com.connect.android.adapters.*
import com.connect.android.databinding.FragmentLikeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LikeFragment : Fragment() {

    private lateinit var binding: FragmentLikeBinding
    private lateinit var likeFragmentArgs: LikeFragmentArgs
    private lateinit var likeAdapter: LikeAdapter

    private val likeViewModel by viewModels<LikeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_like, container, false)

        // Get args using by navArgs property delegate
        getArgs()

        // Toolbar setup
        mToolbarSetup()

        // setup recycler view
        setupRecyclerView()

        // Setup adapter with data
        setupView()

        // Observing live data
        observingData()

        return binding.root
    }

    private fun setupView() {
        likeViewModel.likesData?.observe(viewLifecycleOwner) {
            likeAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun setupRecyclerView() {
        likeAdapter = LikeAdapter(LikedByListener { likedById ->
            likeViewModel.navigateToProfile(likedById)
        })
        binding.likeRv.setHasFixedSize(true)
        binding.likeRv.adapter = likeAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { likeAdapter.retry() }
        )

        likeAdapter.addLoadStateListener { loadState ->
            binding.likePd.isVisible = loadState.source.refresh is LoadState.Loading
            handleError(loadState)
        }
    }

    private fun handleError(loadState: CombinedLoadStates) {
        // getting the error
        val error = when {
            loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
            loadState.append is LoadState.Error -> loadState.append as LoadState.Error
            loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
            else -> null
        }
        error?.let {
            when (it.error.message) {
                "404" -> {
                    Timber.d(it.error.message)
                    likeViewModel.showNoUsersMsg()
                }
                "No Internet" -> {
                    Timber.d(it.error.message)
                    likeViewModel.showNoInternetMsg()
                }
                "500" -> {
                    Timber.d(it.error.message)
                    likeViewModel.showUnknownErrorMsg()
                }
                else -> {
                    Timber.d(it.error.message)
                    Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observingData() {
        /**
         * Should show no likes found
         */
        noLikesFound()

        /**
         * Should show no internet
         */
        noInternet()

        /**
         * Should show unknown error
         */
        unknownError()

        /**
         * Observing if clicked on any list of search users
         */
        clickOnLikedByUser()

        /**
         * Observing, Navigate back to home fragment
         */
        navigateToHome()
    }

    private fun navigateToHome() {
        likeViewModel.navigateToHomeBack.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                likeViewModel.navigateToHomeDone()
            }
        }
    }

    private fun noInternet() {
        likeViewModel.noInternet.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                likeViewModel.showNoInternetMsgDone()
            }
        }
    }

    private fun unknownError() {
        likeViewModel.unknownError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                likeViewModel.showUnknownErrorMsgDone()
            }
        }
    }

    private fun noLikesFound() {
        likeViewModel.noLikes.observe(viewLifecycleOwner) {
            if (it) {
                binding.likeLl.visibility = View.VISIBLE
                likeViewModel.showNoUsersMsgDone()
            }
        }
    }

    private fun clickOnLikedByUser() {
        likeViewModel.navigateToProfileFragment.observe(viewLifecycleOwner) {
            it?.let {
                val action = LikeFragmentDirections.actionLikeFragmentToProfileFragment2(
                    visitedUserId = it,
                    loggedInUserId = likeFragmentArgs.loggedInUserId
                )
                findNavController().navigate(action)
                likeViewModel.navigateToProfileDone()
            }
        }
    }

    private fun mToolbarSetup() {
        binding.likeToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.likeToolbar.title = "Likes"
        binding.likeToolbar.subtitle = likeFragmentArgs.numLikes.toString()

        binding.likeToolbar.setNavigationOnClickListener {
            likeViewModel.navigateToHome()
        }
    }

    private fun getArgs() {
        likeFragmentArgs = LikeFragmentArgs.fromBundle(requireArguments())
    }
}