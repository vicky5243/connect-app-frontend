package com.connect.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.adapters.FooterLoadStateAdapter
import com.connect.android.adapters.NewsfeedAdapter
import com.connect.android.databinding.FragmentHomeBinding
import com.connect.android.utils.Constants
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(), NewsfeedAdapter.OnItemClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var arguments: HomeFragmentArgs
    private lateinit var newsfeedAdapter: NewsfeedAdapter

    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // Get args using by navArgs property delegate
        getArgs()

        // setup recycler view
        setupRecyclerView()

        // Setup adapter with data
        setupView()

        // Click events
        clickEvents()

        // Observing live data
        observingData()

        return binding.root
    }

    private fun clickEvents() {
        // Search icon clicked
        binding.homeSearchIv.setOnClickListener {
            homeViewModel.showSearchFragment()
        }

        // Profile photo icon clicked
        binding.homeUserProfilePicCiv.setOnClickListener {
            arguments.user?.id?.let { uid ->
                Timber.d("uid: $uid")
                homeViewModel.onProfilePhotoClicked(uid)
            }
        }
    }

    private fun observingData() {
        /**
         * Should show no posts found
         */
        noPostsFound()

        /**
         * Should show no internet
         */
        noInternet()

        /**
         * Should show unknown error
         */
        unknownError()

        /**
         * Navigate to Search Fragment
         */
        navigateToSearchFragment()

        /**
         * Navigate to Profile Fragment
         */
        navigateToProfileFragment()

        /**
         * Navigate to Like Fragment
         */
        navigateToLikeFragment()

        /**
         * Navigate to Comment Fragment
         */
        navigateToCommentFragment()

        /**
         * Like or Unlike a post clicked event observing
         */
        likeOrUnlike()

        /**
         * Show like error toast message
         */
        showErrorToast()
    }

    private fun showErrorToast() {
        homeViewModel.showLikeErrorToast.observe(viewLifecycleOwner) {
            Timber.d("it: $it")
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                homeViewModel.showErrorToastDone()
            }
        }
    }

    private fun likeOrUnlike() {
        homeViewModel.hasLiked.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { successRes ->
                        if (successRes.hasLiked) {
                            // Liked
                            Timber.d("LIKED THE POST")
                        } else {
                            // UnLiked
                            Timber.d("UNLIKED THE POST")
                        }
                    }
                }
                is  Resource.Failure -> {
                    it.message?.let { message ->
                        // Show error, if any occurred
                        homeViewModel.showErrorToast(message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun navigateToCommentFragment() {
        homeViewModel.navigateToCommentFragment.observe(viewLifecycleOwner) {
            it?.let { pair ->
                val action = HomeFragmentDirections.actionHomeFragment2ToCommentFragment(
                    pid = pair.first,
                    numComments = pair.second,
                    loggedInUserId = arguments.user?.id!!
                )
                findNavController().navigate(action)
                homeViewModel.navigateToCommentDone()
            }
        }
    }

    private fun navigateToLikeFragment() {
        homeViewModel.navigateToLikeFragment.observe(viewLifecycleOwner) {
            it?.let { pair ->
                val action = HomeFragmentDirections.actionHomeFragment2ToLikeFragment(
                    pid = pair.first,
                    numLikes = pair.second,
                    loggedInUserId = arguments.user?.id!!
                )
                findNavController().navigate(action)
                homeViewModel.navigateToLikeDone()
            }
        }
    }

    private fun navigateToProfileFragment() {
        homeViewModel.navigateToProfileFragment.observe(viewLifecycleOwner) {
            it?.let {
                val action = HomeFragmentDirections.actionHomeFragment2ToProfileFragment2(
                    visitedUserId = it,
                    loggedInUserId = arguments.user?.id!!,
                )
                findNavController().navigate(action)
                homeViewModel.onProfilePhotoClickedDone()
            }
        }
    }

    private fun navigateToSearchFragment() {
        homeViewModel.navigateToSearchFragment.observe(viewLifecycleOwner) {
            if (it) {
                val action = HomeFragmentDirections.actionHomeFragment2ToSearchFragment2(
                    loggedInUserId = arguments.user?.id!!
                )
                findNavController().navigate(action)

                homeViewModel.showSearchFragmentDone()
            }
        }
    }

    private fun unknownError() {
        homeViewModel.unknownError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                homeViewModel.showUnknownErrorMsgDone()
            }
        }
    }

    private fun noInternet() {
        homeViewModel.noInternet.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                homeViewModel.showNoInternetMsgDone()
            }
        }
    }

    private fun noPostsFound() {
        homeViewModel.noPosts.observe(viewLifecycleOwner) {
            if (it) {
                binding.homeLl.visibility = View.VISIBLE
                homeViewModel.showNoPostsMsgDone()
            }
        }
    }

    private fun setupView() {
        homeViewModel.newsfeedPostsData.observe(viewLifecycleOwner) {
            newsfeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun setupRecyclerView() {
        newsfeedAdapter = NewsfeedAdapter(this)
        binding.homeNewsfeedPostsRv.setHasFixedSize(true)
        binding.homeNewsfeedPostsRv.itemAnimator = null
        binding.homeNewsfeedPostsRv.adapter = newsfeedAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { newsfeedAdapter.retry() }
        )

        newsfeedAdapter.addLoadStateListener { loadState ->
            binding.homePd.isVisible = loadState.source.refresh is LoadState.Loading
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
                    homeViewModel.showNoPostsMsg()
                }
                "No Internet" -> {
                    Timber.d(it.error.message)
                    homeViewModel.showNoInternetMsg()
                }
                "500" -> {
                    Timber.d(it.error.message)
                    homeViewModel.showUnknownErrorMsg()
                }
                else -> {
                    Timber.d(it.error.message)
                    Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getArgs() {
        arguments = HomeFragmentArgs.fromBundle(requireArguments())
        Timber.d("User Arguments: ${arguments.user}")

        /**
         * Set user profile pic
         */
        Glide.with(requireContext())
            .load("${Constants.PROFILE_IMAGE_URL}/${arguments.user?.profilePhotoUrl}")
            .into(binding.homeUserProfilePicCiv)
    }

    /**
     * Methods of OnItemClickListener interface
     * Handling single post's click events
     */
    override fun onItemUserProfilePhotoClicked(uid: Long) {
        Timber.d("onItemUserProfilePhotoClicked called!")
        homeViewModel.onProfilePhotoClicked(uid)
    }

    override fun onItemUserUsernameClicked(uid: Long) {
        Timber.d("onItemUserUsernameClicked called!")
        homeViewModel.onProfilePhotoClicked(uid)
    }

    override fun onItemAllLikesClicked(pid: Long, numLikes: Long) {
        Timber.d("onItemAllLikesClicked called! $pid")
        homeViewModel.navigateToLike(pid, numLikes)
    }

    override fun onItemLikeOrUnlikeClicked(pid: Long) {
        Timber.d("onItemLikeOrUnlikeClicked called! $pid")
        homeViewModel.likeOrUnlikeAPost(pid)
    }

    override fun onItemAllCommentsClicked(pid: Long, numComments: Long) {
        Timber.d("onItemAllCommentsClicked called! $pid")
        homeViewModel.navigateToComment(pid, numComments)
    }
}