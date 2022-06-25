package com.connect.android.ui.profile.post

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
import com.connect.android.adapters.FooterLoadStateAdapter
import com.connect.android.adapters.NewsfeedAdapter
import com.connect.android.databinding.FragmentPostBinding
import com.connect.android.utils.Resource
import com.connect.android.utils.smoothSnapToPosition
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PostFragment : Fragment(), NewsfeedAdapter.OnItemClickListener {

    private lateinit var binding: FragmentPostBinding
    private lateinit var postFragmentArgs: PostFragmentArgs
    private lateinit var newsfeedAdapter: NewsfeedAdapter

    private val postViewModel by viewModels<PostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post, container, false)

        // Get args using by navArgs property delegate
        getArgs()

        // Setting up the viewModel
        viewModelSetup()

        // setup recycler view
        setupRecyclerView()

        // Setup adapter with data
        setupView()

        // Toolbar setup
        mToolbarSetup()

        // Observing live data
        observingData()

        return binding.root
    }

    private fun setupRecyclerView() {
        newsfeedAdapter = NewsfeedAdapter(this)
        binding.postNewsfeedPostsRv.setHasFixedSize(true)
        binding.postNewsfeedPostsRv.itemAnimator = null
        binding.postNewsfeedPostsRv.adapter = newsfeedAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { newsfeedAdapter.retry() }
        )

        newsfeedAdapter.addLoadStateListener { loadState ->
            binding.postPd.isVisible = loadState.source.refresh is LoadState.Loading
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

        // Scroll to certain position
        if (error == null) {
            if (loadState.source.refresh is LoadState.NotLoading) {
                binding.postNewsfeedPostsRv.smoothSnapToPosition(postFragmentArgs.scrollToPos)
            }
        } else {
            when (error.error.message) {
                "404" -> {
                    Timber.d(error.error.message)
                    postViewModel.showNoPostsMsg()
                }
                "No Internet" -> {
                    Timber.d(error.error.message)
                    postViewModel.showNoInternetMsg()
                }
                "500" -> {
                    Timber.d(error.error.message)
                    postViewModel.showUnknownErrorMsg()
                }
                else -> {
                    Timber.d(error.error.message)
                    Toast.makeText(requireContext(), error.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        postViewModel.getPostsData.observe(viewLifecycleOwner) {
            newsfeedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun observingData() {
        /**
         * Observing to navigate back to previous destination
         */
        navigateBackToPreviousDestination()

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

    private fun likeOrUnlike() {
        postViewModel.hasLiked.observe(viewLifecycleOwner) {
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
                        postViewModel.showErrorToast(message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun showErrorToast() {
        postViewModel.showLikeErrorToast.observe(viewLifecycleOwner) {
            Timber.d("it: $it")
            it?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                postViewModel.showErrorToastDone()
            }
        }
    }

    private fun navigateToCommentFragment() {
        postViewModel.navigateToCommentFragment.observe(viewLifecycleOwner) {
            it?.let { pair ->
                val action = PostFragmentDirections.actionPostFragmentToCommentFragment(
                    pid = pair.first,
                    numComments = pair.second,
                    loggedInUserId = postFragmentArgs.loggedInUserId
                )
                findNavController().navigate(action)
                postViewModel.navigateToCommentDone()
            }
        }
    }

    private fun navigateToLikeFragment() {
        postViewModel.navigateToLikeFragment.observe(viewLifecycleOwner) {
            it?.let { pair ->
                val action = PostFragmentDirections.actionPostFragmentToLikeFragment(
                    pid = pair.first,
                    numLikes = pair.second,
                    loggedInUserId = postFragmentArgs.loggedInUserId
                )
                findNavController().navigate(action)
                postViewModel.navigateToLikeDone()
            }
        }
    }

    private fun navigateToProfileFragment() {
        postViewModel.navigateToProfileFragment.observe(viewLifecycleOwner) {
            it?.let {
                val action = PostFragmentDirections.actionPostFragmentToProfileFragment2(
                    visitedUserId = it,
                    loggedInUserId = postFragmentArgs.loggedInUserId
                )
                findNavController().navigate(action)
                postViewModel.onProfilePhotoClickedDone()
            }
        }
    }

    private fun unknownError() {
        postViewModel.unknownError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                postViewModel.showUnknownErrorMsgDone()
            }
        }
    }

    private fun noInternet() {
        postViewModel.noInternet.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                postViewModel.showNoInternetMsgDone()
            }
        }
    }

    private fun noPostsFound() {
        postViewModel.noPosts.observe(viewLifecycleOwner) {
            if (it) {
                binding.postLl.visibility = View.VISIBLE
                postViewModel.showNoPostsMsgDone()
            }
        }
    }

    private fun navigateBackToPreviousDestination() {
        postViewModel.navigateBackToPreviousDest.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                postViewModel.doneNavigateToPreviousDest()
            }
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = postViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun mToolbarSetup() {
        // Show the navigation up icon
        binding.postToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.postToolbar.title = getString(R.string.posts)
        binding.postToolbar.subtitle = postFragmentArgs.numPosts.toString()

        // Click event on navigation up
        binding.postToolbar.setNavigationOnClickListener {
            postViewModel.navigateToPreviousDest()
        }
    }

    private fun getArgs() {
        postFragmentArgs = PostFragmentArgs.fromBundle(requireArguments())
    }

    /**
     * Methods of OnItemClickListener interface
     * Handling single post's click events
     */
    override fun onItemUserProfilePhotoClicked(uid: Long) {
        Timber.d("onItemUserProfilePhotoClicked called!")
        postViewModel.onProfilePhotoClicked(uid)
    }

    override fun onItemUserUsernameClicked(uid: Long) {
        Timber.d("onItemUserUsernameClicked called!")
        postViewModel.onProfilePhotoClicked(uid)
    }

    override fun onItemAllLikesClicked(pid: Long, numLikes: Long) {
        Timber.d("onItemAllLikesClicked called! $pid")
        postViewModel.navigateToLike(pid, numLikes)
    }

    override fun onItemLikeOrUnlikeClicked(pid: Long) {
        Timber.d("onItemLikeOrUnlikeClicked called! $pid")
        postViewModel.likeOrUnlikeAPost(pid)
    }

    override fun onItemAllCommentsClicked(pid: Long, numComments: Long) {
        Timber.d("onItemAllCommentsClicked called! $pid")
        postViewModel.navigateToComment(pid, numComments)
    }
}