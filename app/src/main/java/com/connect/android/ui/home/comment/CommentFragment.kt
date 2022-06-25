package com.connect.android.ui.home.comment

import android.os.Bundle
import android.view.KeyEvent
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
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.adapters.*
import com.connect.android.databinding.FragmentCommentBinding
import com.connect.android.utils.Constants
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CommentFragment : Fragment(), CommentAdapter.OnCommentItemClickListener {

    private lateinit var binding: FragmentCommentBinding
    private lateinit var commentFragmentArgs: CommentFragmentArgs
    private lateinit var commentAdapter: CommentAdapter

    private val commentViewModel by viewModels<CommentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment, container, false)

        // Get args using by navArgs property delegate
        getArgs()

        // Setting up the viewModel
        viewModelSetup()

        // Toolbar setup
        mToolbarSetup()

        // setup recycler view
        setupRecyclerView()

        // Setup adapter with data
        setupView()

        // Observing live data
        observingData()

        // Keyboard listeners
        androidKeyListenerEvents()

        return binding.root
    }

    private fun androidKeyListenerEvents() {
        /**
         * When user submit the form from keyboard enter pressed
         * and button is enabled then, submit the form
         */
        binding.commentEt.setOnKeyListener { _, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
                if (commentViewModel.isCommentPostTvEnabled.value == true) {
                    commentViewModel.commentOnAPost()
                }
            }
            false
        }
    }

    private fun setupView() {
        commentViewModel.commentsData?.observe(viewLifecycleOwner) {
            commentAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(this)
        binding.commentRv.setHasFixedSize(true)
        binding.commentRv.adapter = commentAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { commentAdapter.retry() }
        )

        commentAdapter.addLoadStateListener { loadState ->
            binding.commentPd.isVisible = loadState.source.refresh is LoadState.Loading
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
                    commentViewModel.showNoUsersMsg()
                }
                "No Internet" -> {
                    Timber.d(it.error.message)
                    commentViewModel.showNoInternetMsg()
                }
                "500" -> {
                    Timber.d(it.error.message)
                    commentViewModel.showUnknownErrorMsg()
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
         * Retrieve online user profile dp
         */
        onlineUserProfileDP()

        /**
         * Should show no comments found
         */
        noCommentsFound()

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
        clickOnCommentedByUser()

        /**
         * Observing, Navigate back to home fragment
         */
        navigateToHome()

        /**
         * Observing that, has been commented
         */
        hasBeenCommented()

        /**
         * If comment has created successfully show the toast
         */
        showSuccessToast()

        /**
         * If comment hasn't created successfully show the error toast
         */
        showUnSuccessToast()

        /**
         * Observing has comment post tv enabled or not
         * if tv is disabled set alpha = 0.3f
         * else set alpha = 1f
         */
        isCommentPostTvEnabled()

        /**
         * Observing has comment tv clicked or not
         * if comment tv clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isCommentPostTvClicked()
    }

    private fun onlineUserProfileDP() {
        commentViewModel.onlineUserProfilePhotoUrl.observe(viewLifecycleOwner) {
            it?.let { url ->
                /**
                 * Set user profile pic
                 */
                Glide.with(requireContext())
                    .load("${Constants.PROFILE_IMAGE_URL}/${url}")
                    .into(binding.commentByCiv)
            }
        }
    }

    private fun isCommentPostTvClicked() {
        commentViewModel.isCommentPostTvClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.commentEt.isEnabled = false
                binding.commentEt.alpha = 0.7f
            } else {
                binding.commentEt.isEnabled = true
                binding.commentEt.alpha = 1f
            }
        }
    }

    private fun isCommentPostTvEnabled() {
        commentViewModel.isCommentPostTvEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.commentPostTv.alpha = 1f
            } else {
                binding.commentPostTv.alpha = 0.3f
            }
        }
    }

    private fun showUnSuccessToast() {
        commentViewModel.showCommentedUnSuccessToast.observe(viewLifecycleOwner) {
            it?.let { errorMsg ->
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                commentViewModel.showErrorToastDone()
            }
        }
    }

    private fun showSuccessToast() {
        commentViewModel.showCommentedSuccessToast.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "You commented successfully.", Toast.LENGTH_LONG)
                    .show()
                commentViewModel.showToastDone()
            }
        }
    }

    private fun hasBeenCommented() {
        commentViewModel.hasCommented.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // Show loading
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    hideProgressIndicator()
                    commentViewModel.showToast()
                }
                is Resource.Failure -> {
                    // Show error message
                    hideProgressIndicator()
                    it.message?.let { errorMessage ->
                        commentViewModel.showErrorToast(errorMessage)
                    }
                }
            }
        }
    }

    private fun hideProgressIndicator() {
        binding.commentCommentTextPd.visibility = View.GONE
        binding.commentPostTv.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.commentCommentTextPd.visibility = View.VISIBLE
        binding.commentPostTv.visibility = View.GONE
    }

    private fun noInternet() {
        commentViewModel.noInternet.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                commentViewModel.showNoInternetMsgDone()
            }
        }
    }

    private fun unknownError() {
        commentViewModel.unknownError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                commentViewModel.showUnknownErrorMsgDone()
            }
        }
    }

    private fun noCommentsFound() {
        commentViewModel.noComments.observe(viewLifecycleOwner) {
            if (it) {
                binding.commentLl.visibility = View.VISIBLE
                commentViewModel.showNoUsersMsgDone()
            }
        }
    }

    private fun clickOnCommentedByUser() {
        commentViewModel.navigateToProfileFragment.observe(viewLifecycleOwner) {
            it?.let {
                val action = CommentFragmentDirections.actionCommentFragmentToProfileFragment2(
                    visitedUserId = it,
                    loggedInUserId = commentFragmentArgs.loggedInUserId
                )
                findNavController().navigate(action)
                commentViewModel.navigateToProfileDone()
            }
        }
    }

    private fun navigateToHome() {
        commentViewModel.navigateToHomeBack.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                commentViewModel.navigateToHomeDone()
            }
        }
    }

    private fun mToolbarSetup() {
        binding.commentToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.commentToolbar.title = "Comments"
        binding.commentToolbar.subtitle = commentFragmentArgs.numComments.toString()

        binding.commentToolbar.setNavigationOnClickListener {
            commentViewModel.navigateToHome()
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = commentViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun getArgs() {
        commentFragmentArgs = CommentFragmentArgs.fromBundle(requireArguments())
    }

    /**
     * Methods of OnCommentItemClickListener interface
     * Handling single comment's click events
     */
    override fun onItemUserProfilePhotoClicked(uid: Long) {
        commentViewModel.navigateToProfile(uid)
    }
}