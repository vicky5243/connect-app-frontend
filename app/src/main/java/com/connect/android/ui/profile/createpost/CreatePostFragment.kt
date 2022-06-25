package com.connect.android.ui.profile.createpost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.databinding.FragmentCreatePostBinding
import com.connect.android.utils.Resource
import com.connect.android.utils.getFileName
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@AndroidEntryPoint
class CreatePostFragment : Fragment() {

    private lateinit var binding: FragmentCreatePostBinding
    private val createPostViewModel by viewModels<CreatePostViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_post, container, false)

        // Setting up the viewModel
        viewModelSetup()

        // Toolbar setup
        mToolbarSetup()

        // Observing live data
        observingData()

        return binding.root
    }

    private fun observingData() {
        /**
         * Observing to navigate back to previous destination
         */
        navigateBackToPreviousDestination()

        /**
         * Observing, opening the gallery to pick image
         */
        openGallery()

        /**
         * Observing has create post btn enabled or not
         * if button is disabled set alpha = 0.1f
         * else set alpha = 1f
         */
        isCreatePostBtnEnabled()

        /**
         * Observing has create post btn clicked or not
         * if sign in btn clicked then disable all fields and set alpha = 0.7f
         * else enable all fields and set alpha = 1f
         */
        isCreatePostBtnClicked()

        /**
         * Has been post created
         */
        hasPostCreated()

        /**
         * Post create success toast
         */
        showSuccessToast()
    }

    private fun showSuccessToast() {
        createPostViewModel.showCreatePostSuccessToast.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                createPostViewModel.showToastDone()
            }
        }
    }

    private fun hasPostCreated() {
        createPostViewModel.hasPostCreated.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    showProgressIndicator()
                }
                is Resource.Success -> {
                    hideProgressIndicator()
                    it.data?.let { data ->
                        createPostViewModel.showToast(data.message)
                    }
                }
                is Resource.Failure -> {
                    hideProgressIndicator()
                    it.message?.let { errorMessage ->
                        val action =
                            CreatePostFragmentDirections.actionGlobalCustomDialog(
                                getString(R.string.errorDialogTitle),
                                errorMessage
                            )
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    private fun isCreatePostBtnEnabled() {
        createPostViewModel.hasPostBtnEnabled.observe(viewLifecycleOwner) {
            if (it) {
                binding.createPostBtn.alpha = 1f
            } else {
                binding.createPostBtn.alpha = 0.1f
            }
        }
    }

    private fun isCreatePostBtnClicked() {
        createPostViewModel.hasPostBtnClicked.observe(viewLifecycleOwner) { btnClicked ->
            if (btnClicked) {
                binding.createPostTitleEt.isEnabled = false
                binding.createPostDescEt.isEnabled = false
                binding.createPostSelectIv.isEnabled = false
                binding.createPostTitleEt.alpha = 0.7f
                binding.createPostDescEt.alpha = 0.7f
                binding.createPostSelectIv.alpha = 0.7f
            } else {
                binding.createPostTitleEt.isEnabled = true
                binding.createPostDescEt.isEnabled = true
                binding.createPostSelectIv.isEnabled = true
                binding.createPostTitleEt.alpha = 1f
                binding.createPostDescEt.alpha = 1f
                binding.createPostSelectIv.alpha = 1f
            }
        }
    }

    private fun openGallery() {
        createPostViewModel.openGallery.observe(viewLifecycleOwner) {
            if (it) {
                pickImageFile()
                createPostViewModel.navigateToGalleryDone()
            }
        }
    }

    private val contract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Timber.d("URI: $it")
        /**
         * Binding user profile pic
         */
        it?.let { uri ->
            val parcelFileDescriptor =
                requireContext().contentResolver.openFileDescriptor(uri, "r", null)
            val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
            val file =
                File(requireContext().cacheDir, requireContext().contentResolver.getFileName(uri))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            createPostViewModel.saveImageFile(file)
            binding.chooseAnImageLibraryIv.visibility = View.GONE
            Glide.with(requireContext())
                .load(uri)
                .into(binding.createPostSelectIv)
        }
    }

    private fun pickImageFile() {
        Timber.d("pickImageFile called!")

        contract.launch("image/*")
    }

    private fun navigateBackToPreviousDestination() {
        createPostViewModel.navigateBackToPreviousDest.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().popBackStack()
                createPostViewModel.doneNavigateToPreviousDest()
            }
        }
    }

    private fun mToolbarSetup() {
        // Show the navigation up icon
        binding.createPostToolbar.setNavigationIcon(R.drawable.ic_back)

        // Set toolbar title
        binding.createPostToolbar.title = getString(R.string.create_post)

        // Click event on navigation up
        binding.createPostToolbar.setNavigationOnClickListener {
            createPostViewModel.navigateToPreviousDest()
        }
    }

    private fun viewModelSetup() {
        // connecting xml and viewModel class
        binding.viewModel = createPostViewModel
        // enabling observation of livedata from xml file
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun hideProgressIndicator() {
        binding.createPostPd.visibility = View.GONE
        binding.createPostBtn.visibility = View.VISIBLE
    }

    private fun showProgressIndicator() {
        binding.createPostPd.visibility = View.VISIBLE
        binding.createPostBtn.visibility = View.GONE
    }
}