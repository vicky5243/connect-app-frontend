package com.connect.android.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.connect.android.R
import com.connect.android.adapters.FooterLoadStateAdapter
import com.connect.android.adapters.SearchUserAdapter
import com.connect.android.adapters.SearchUserListener
import com.connect.android.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchUserArgs: SearchFragmentArgs
    private lateinit var searchUserAdapter: SearchUserAdapter
    private val searchUserViewModel by viewModels<SearchUserViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

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

        // Search new users
        searchNewUsers()

        return binding.root
    }

    private fun searchNewUsers() {
        binding.searchUserTextEt.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                val newQueryString = binding.searchUserTextEt.text.toString()
                searchUserViewModel.performSearchUsers(newQueryString)
            }
            false
        }
    }

    private fun setupView() {
        searchUserViewModel.usersData.observe(viewLifecycleOwner) {
            searchUserAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun mToolbarSetup() {
        binding.searchUserToolbar.setNavigationIcon(R.drawable.ic_back)

        binding.searchUserToolbar.setNavigationOnClickListener {
            searchUserViewModel.navigateToHome()
        }
    }

    private fun getArgs() {
        searchUserArgs = SearchFragmentArgs.fromBundle(requireArguments())
    }

    private fun setupRecyclerView() {
        searchUserAdapter = SearchUserAdapter(SearchUserListener { searchUserId ->
            searchUserViewModel.onSearchUserClicked(searchUserId)
        })
        binding.searchUserRv.setHasFixedSize(true)
        binding.searchUserRv.adapter = searchUserAdapter.withLoadStateFooter(
            footer = FooterLoadStateAdapter { searchUserAdapter.retry() }
        )

        searchUserAdapter.addLoadStateListener { loadState ->
            binding.searchUserPd.isVisible = loadState.source.refresh is LoadState.Loading
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
                    hideRecyclerView()
                    searchUserViewModel.showNoUsersMsg()
                }
                "No Internet" -> {
                    Timber.d(it.error.message)
                    searchUserViewModel.showNoInternetMsg()
                }
                "Req" -> {
                    /**
                     * Do Nothing, initial edit text is blank
                     */
                    Timber.d("Field required")
                }
                "500" -> {
                    Timber.d(it.error.message)
                    searchUserViewModel.showUnknownErrorMsg()
                }
                else -> {
                    Timber.d(it.error.message)
                    Toast.makeText(requireContext(), it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hideRecyclerView() {
        binding.searchUserRv.visibility = View.GONE
    }

    private fun showRecyclerView() {
        binding.searchUserRv.visibility = View.VISIBLE
    }

    private fun observingData() {
        /**
         * Should show no users found
         */
        noUsersFound()

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
        clickOnSearchUsers()

        /**
         * Observing, Navigate back to home fragment
         */
        navigateToHome()

        /**
         * Observing , edit text (search query)
         */
        searchQueryEditText()
    }

    private fun searchQueryEditText() {
        searchUserViewModel.currentQuery.observe(viewLifecycleOwner) {
            it?.let {
                showRecyclerView()
                hideNoUsersViews()
            }
        }
    }

    private fun hideNoUsersViews() {
        binding.searchUserLl.visibility = View.GONE
    }

    private fun navigateToHome() {
        searchUserViewModel.navigateToHomeBack.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                searchUserViewModel.navigateToHomeDone()
            }
        }
    }

    private fun noInternet() {
        searchUserViewModel.noInternet.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG)
                    .show()
                searchUserViewModel.showNoInternetMsgDone()
            }
        }
    }

    private fun unknownError() {
        searchUserViewModel.unknownError.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                searchUserViewModel.showUnknownErrorMsgDone()
            }
        }
    }

    private fun noUsersFound() {
        searchUserViewModel.noUsers.observe(viewLifecycleOwner) {
            if (it) {
                binding.searchUserLl.visibility = View.VISIBLE
                searchUserViewModel.showNoUsersMsgDone()
            }
        }
    }

    private fun clickOnSearchUsers() {
        searchUserViewModel.navigateToProfileFragment.observe(viewLifecycleOwner) {
            it?.let {
                val action = SearchFragmentDirections.actionSearchFragment2ToProfileFragment2(
                    visitedUserId = it,
                    loggedInUserId = searchUserArgs.loggedInUserId
                )
                findNavController().navigate(action)
                searchUserViewModel.onSearchUserNavigated()
            }
        }
    }

    private fun viewModelSetup() {

        // connecting xml and viewModel class
        binding.viewModel = searchUserViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }
}