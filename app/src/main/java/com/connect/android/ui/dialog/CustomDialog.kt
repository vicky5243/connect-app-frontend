package com.connect.android.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.databinding.DialogLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomDialog : DialogFragment() {

    private lateinit var binding: DialogLayoutBinding
    private lateinit var customDialogArgs: CustomDialogArgs

    private val customDialogViewModel by viewModels<CustomDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_round_corner)
        dialog?.setCancelable(false)
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_layout, container, false)

        // Get args using by navArgs property delegate
        getArgs()

        // Observing live data
        observingData()

        // Handling click events
        clickEvents()

        return binding.root
    }

    private fun observingData() {
        customDialogViewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                val action = CustomDialogDirections.actionCustomDialogToLoadingIndicatorDialog2(
                    getString(R.string.logging_out)
                )
                findNavController().navigate(action)
            }
        }

        customDialogViewModel.loggedOut.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getArgs() {
        customDialogArgs = CustomDialogArgs.fromBundle(requireArguments())
        // Set dynamically dialog fragment title and message
        binding.dialogTitleTv.text = customDialogArgs.title
        if (customDialogArgs.message != null) {
            binding.dialogMessageTv.text = customDialogArgs.message
            binding.dialogMessageTv.visibility = View.VISIBLE
        }

        if (customDialogArgs.isLogoutAction) {
            binding.dialogLl1.visibility = View.GONE
            binding.dialogLl2.visibility = View.VISIBLE
            binding.dialogLl3.visibility = View.VISIBLE
        } else {
            binding.dialogLl2.visibility = View.GONE
            binding.dialogLl3.visibility = View.GONE
            binding.dialogLl1.visibility = View.VISIBLE
        }
    }

    private fun clickEvents() {
        /**
         * Dismiss the dialog fragment
         */
        binding.dialogLl1.setOnClickListener {
            dialog?.dismiss()
        }

        binding.dialogLl3.setOnClickListener {
            dialog?.dismiss()
        }

        /**
         * logging out the user
         */
        binding.dialogLl2.setOnClickListener {
            customDialogViewModel.logOutTheUser()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.74).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }
}