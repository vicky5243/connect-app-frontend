package com.connect.android.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.connect.android.R
import com.connect.android.databinding.DialogLoadingIndicatorLayoutBinding
import kotlinx.coroutines.delay

class LoadingIndicatorDialog : DialogFragment() {

    private lateinit var binding: DialogLoadingIndicatorLayoutBinding
    private lateinit var loadingIndicatorDialogArgs: LoadingIndicatorDialogArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_round_corner)
        dialog?.setCancelable(false)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_loading_indicator_layout,
            container,
            false
        )

        // Get args using by navArgs property delegate
        getArgs()

        lifecycleScope.launchWhenStarted {
            val action = LoadingIndicatorDialogDirections.actionLoadingIndicatorDialog2ToSigninFragment2()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun getArgs() {
        loadingIndicatorDialogArgs = LoadingIndicatorDialogArgs.fromBundle(requireArguments())

        // Set message
        binding.dialogLoadingMessageTv.text = loadingIndicatorDialogArgs.message
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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