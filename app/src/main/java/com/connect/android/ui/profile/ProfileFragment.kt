package com.connect.android.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.adapters.FooterLoadStateAdapter
import com.connect.android.adapters.ProfilePostAdapter
import com.connect.android.databinding.FragmentProfileBinding
import com.connect.android.models.res.GetUser
import com.connect.android.utils.Constants
import com.connect.android.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : Fragment(), ProfilePostAdapter.OnItemClickListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileFragmentArgs: ProfileFragmentArgs
    private lateinit var profilePostAdapter: ProfilePostAdapter

    private val profileViewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

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

        return binding.root
    }

    private fun setupRecyclerView() {
        profilePostAdapter = ProfilePostAdapter(this)
        binding.profilePostsRv.setHasFixedSize(false)
        binding.profilePostsRv.isNestedScrollingEnabled = false
        binding.profilePostsRv.layoutManager =
            GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
        binding.profilePostsRv.adapter = profilePostAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { profilePostAdapter.retry() }
        )

        profilePostAdapter.addLoadStateListener { loadState ->
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
                    profileViewModel.showNoPostsMsg()
                }
                else -> {
                    Timber.d(it.error.message)
                    Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        profileViewModel.getPostsData.observe(viewLifecycleOwner) {
            profilePostAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun getArgs() {
        profileFragmentArgs = ProfileFragmentArgs.fromBundle(requireArguments())
    }

    private fun viewModelSetup() {

        // connecting xml and viewModel class
        binding.viewModel = profileViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun observingData() {
        /**
         * Observing if visited profile user details has been fetched or not
         */
        fetchedVisitedProfileUser()

        /**
         * Observing to navigate back to previous destination
         */
        navigateBackToPreviousDestination()

        /**
         * Observing to navigation setting fragment
         */
        navigateToCreatePostFragment()

        /**
         * Observing to navigation setting fragment
         */
        navigateToSettingsFragment()

        /**
         * Observing to navigation edit profile fragment
         */
        navigateToEditProfileFragment()

        /**
         * Observing to navigation follower fragment
         */
//        navigateToFollowerFragment()

        /**
         * Observing to navigation post fragment
         */
        navigateToPostFragment()

        /**
         * Observing to navigation following fragment
         */
//        navigateToFollowingFragment()

        /**
         * Observing, has user follow or unfollow the user or not
         */
        followTheUser()

        /**
         * Should show no posts found
         */
        noPostsFound()

        /**
         * Profile posts item clicked
         */
        profilePostsItemClicked()
    }

    private fun profilePostsItemClicked() {
        profileViewModel.onPostItemClickedPos.observe(viewLifecycleOwner) {
            it?.let {
                val numPosts = profileViewModel.visitedProfileUser.value?.data?.numPosts
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToPostFragment(
                        visitedUserId = profileFragmentArgs.visitedUserId,
                        loggedInUserId = profileFragmentArgs.loggedInUserId,
                        scrollToPos = it,
                        numPosts = numPosts ?: 0
                    )
                findNavController().navigate(action)
                profileViewModel.onPostItemClickedDone()
            }
        }
    }

    private fun noPostsFound() {
        profileViewModel.noPosts.observe(viewLifecycleOwner) {
            if (it) {
                binding.profileNoPostsLl.visibility = View.VISIBLE
                profileViewModel.showNoPostsMsgDone()
            }
        }
    }

    private fun navigateToPostFragment() {
        profileViewModel.navigatePostFragment.observe(viewLifecycleOwner) {
            it?.let { visitedUserId ->
                val numPosts = profileViewModel.visitedProfileUser.value?.data?.numPosts
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToPostFragment(
                        visitedUserId = visitedUserId,
                        loggedInUserId = profileFragmentArgs.loggedInUserId,
                        scrollToPos = 0,
                        numPosts = numPosts ?: 0
                    )
                findNavController().navigate(action)

                profileViewModel.navigateToPostDone()
            }
        }
    }

    private fun followTheUser() {
        profileViewModel.hasFollow.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    Timber.d("Loading...")
                    binding.profileFollowBtn.visibility = View.GONE
                    binding.profileFollowPd.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    Timber.d("Success: ${it.data?.message}")
                    it.data?.let { _ ->
                        binding.profileFollowBtn.visibility = View.VISIBLE
                        binding.profileFollowPd.visibility = View.GONE

                        if (binding.profileFollowBtn.text == getString(R.string.follow) ||
                            binding.profileFollowBtn.text == getString(R.string.follow_back)
                        ) {
                            /**
                             * User start following,
                             * send text to unfollow
                             */
                            binding.profileFollowBtn.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.black
                                )
                            )
                            binding.profileFollowBtn.setBackgroundResource(R.drawable.gray_button_background)
                            binding.profileFollowBtn.text = getString(R.string.unfollow)
                        } else {
                            /**
                             * User unfollowed,
                             * send text to follow or follow back
                             */
                            binding.profileFollowBtn.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.white
                                )
                            )
                            binding.profileFollowBtn.setBackgroundResource(R.drawable.button_background)
                            if (binding.profileFollowBtn.text == getString(R.string.follow)) {
                                binding.profileFollowBtn.text = getString(R.string.follow)
                            } else {
                                binding.profileFollowBtn.text = getString(R.string.follow_back)
                            }
                        }
                    }
                }
                is Resource.Failure -> {
                    Timber.d("Failure: ${it.message}")
                    it.message?.let { errorMessage ->
                        binding.profileFollowBtn.visibility = View.VISIBLE
                        binding.profileFollowPd.visibility = View.GONE
                        val action =
                            ProfileFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun navigateToEditProfileFragment() {
        profileViewModel.navigateEditProfileFragment.observe(viewLifecycleOwner) {
            if (it) {
                val action = ProfileFragmentDirections.actionProfileFragment2ToEditProfileFragment()
                findNavController().navigate(action)

                profileViewModel.doneNavigateToEditProfile()
            }
        }
    }

    private fun navigateBackToPreviousDestination() {
        profileViewModel.navigateBackToPreviousDest.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                profileViewModel.doneNavigateToPreviousDest()
            }
        }
    }

    private fun navigateToCreatePostFragment() {
        profileViewModel.navigateCreatePostFragment.observe(viewLifecycleOwner) {
            if (it) {
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToCreatePostFragment()
                findNavController().navigate(action)
                profileViewModel.doneNavigateToCreatePost()
            }
        }
    }

    private fun navigateToSettingsFragment() {
        profileViewModel.navigateSettingsFragmentBoolean.observe(viewLifecycleOwner) {
            if (it) {
                val action =
                    ProfileFragmentDirections.actionProfileFragment2ToSettingsFragment()
                findNavController().navigate(action)
                profileViewModel.doneNavigateToSettings()
            }
        }
    }

    private fun fetchedVisitedProfileUser() {
        profileViewModel.visitedProfileUser.observe(viewLifecycleOwner) { visitedUser ->
            when (visitedUser) {
                is Resource.Loading -> {
                    Timber.d("Loading...")
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    Timber.d("Success... ${visitedUser.data}")
                    visitedUser.data?.let { user ->
                        // init click listeners
                        initClickListeners(user.id)
                        setTheUserDetailsToViews(user)
                    }
                }
                is Resource.Failure -> {
                    Timber.d("Failure... ${visitedUser.message}")
                    visitedUser.message?.let { errorMessage ->
                        showErrorDialog(errorMessage)
                    }
                }
            }
        }
    }

    private fun initClickListeners(visitedUserId: Long) {
        // Post click listener
        binding.constraintLayout2.setOnClickListener {
            profileViewModel.navigateToPost(visitedUserId)
        }
    }

    private fun setTheUserDetailsToViews(user: GetUser) {
        // Hide all views
        binding.profilePd.visibility = View.GONE
        binding.profileContainerLl2.visibility = View.GONE
        binding.profileToolbar.visibility = View.VISIBLE
        // Show user details
        bindingUserDataDetails(user)
        binding.profileContainerLl.visibility = View.VISIBLE
    }

    private fun bindingUserDataDetails(user: GetUser) {
        /**
         * Setting user's username as title of toolbar
         */
        binding.profileToolbar.title = user.username

        /**
         * Binding user profile pic
         */
        Glide.with(requireContext())
            .load("${Constants.PROFILE_IMAGE_URL}/${user.profilePhotoUrl}")
            .into(binding.profileImageCiv)

        /**
         * Binding full name if user has
         * else hide the view
         */
        if (user.fullname != null) {
            binding.profileFullnameTv.text = user.fullname
        } else {
            // This user doesn't have fullname
            // hide the fullname view
            binding.profileFullnameTv.visibility = View.GONE
        }

        /**
         * Binding relationship status if user has
         * else hide the view
         */
        if (user.relationshipStatus != null) {
            binding.profileRelationshipStatusTv.text = user.relationshipStatus
        } else {
            // This user doesn't have relationship status
            // hide the relationship status view
            binding.profileRelationshipStatusTv.visibility = View.GONE
        }

        /**
         * Binding Number of posts, followers and following
         */
        binding.profileNumPostsTv.text = user.numPosts.toString()
        binding.profileNumFollowersTv.text = user.numFollowers.toString()
        binding.profileNumFollowingTv.text = user.numFollowees.toString()

        /**
         * Checking if logged in user is equal to visited profile user or not
         */
        if (user.id != profileFragmentArgs.loggedInUserId) {
            // Both are different users
            if (user.isFollower && !user.isFollowee) {
                // visited profile user is following to logged in user
                // but logged in user isn't following to visited profile user
                // set the button as "Follow Back"
                binding.profileFollowBtn.text = getString(R.string.follow_back)
            } else if ((user.isFollower && user.isFollowee) || (!user.isFollower && user.isFollowee)) {
                // visited profile user is following to logged in user
                // and logged in user is following to visited profile user as well
                // set the button as "UnFollow"
                binding.profileFollowBtn.text = getString(R.string.unfollow)
                binding.profileFollowBtn.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.profileFollowBtn.setBackgroundResource(R.drawable.gray_button_background)
            } else {
                // visited profile user isn't following to logged in user
                // and logged in user isn't following to visited profile user as well
                // set the button as "Follow"
                binding.profileFollowBtn.text = getString(R.string.follow)
            }

            binding.profileEditBtn.visibility = View.GONE
            binding.profileFollowContainerRl.visibility = View.VISIBLE

        } else {
            // Both are same users
            binding.profileFollowContainerRl.visibility = View.GONE
            binding.profileEditBtn.visibility = View.VISIBLE
        }
    }

    private fun showErrorDialog(errorMessage: String) {
        val action =
            ProfileFragmentDirections.actionGlobalCustomDialog(
                getString(R.string.errorDialogTitle),
                errorMessage
            )
        findNavController().navigate(action)

        // Hide all views
        binding.profilePd.visibility = View.GONE
        binding.profileToolbar.visibility = View.GONE
        binding.profileContainerLl.visibility = View.GONE
        // Show retry btn
        binding.profileContainerLl2.visibility = View.VISIBLE
    }

    private fun mToolbarSetup() {
        /**
         * Inflating menu
         */
        binding.profileToolbar.inflateMenu(R.menu.profile_app_bar_menu)

        /**
         * Menu item click events
         */
        binding.profileToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.add_post -> {
                    // Navigate to settings screen
                    profileViewModel.navigateToCreatePost()
                    true
                }
                R.id.setting -> {
                    // Save profile changes
                    profileViewModel.navigateToSettings()
                    true
                }
                else -> false
            }
        }

        // Show the navigation up icon
        binding.profileToolbar.setNavigationIcon(R.drawable.ic_back)

        // Click event on navigation up
        binding.profileToolbar.setNavigationOnClickListener {
            profileViewModel.navigateToPreviousDest()
        }

        // Visited to other user profile
        if (profileFragmentArgs.visitedUserId != profileFragmentArgs.loggedInUserId) {
            binding.profileToolbar.menu.findItem(R.id.add_post).isVisible = false
            binding.profileToolbar.menu.findItem(R.id.setting).isVisible = false
        }
        // Visited to own profile
        else {
            binding.profileToolbar.menu.findItem(R.id.add_post).isVisible = true
            binding.profileToolbar.menu.findItem(R.id.setting).isVisible = true
        }
    }

    private fun showProgressIndicator() {
        // Hide all views
        binding.profileToolbar.visibility = View.GONE
        binding.profileContainerLl.visibility = View.GONE
        binding.profileContainerLl2.visibility = View.GONE
        // Show progress indicator
        binding.profilePd.visibility = View.VISIBLE
    }

    /**
     * Methods of OnItemClickListener interface
     * Handling single post's click events
     */
    override fun onItemClicked(itemPosition: Int) {
        profileViewModel.onPostItemClicked(itemPosition)
    }
}