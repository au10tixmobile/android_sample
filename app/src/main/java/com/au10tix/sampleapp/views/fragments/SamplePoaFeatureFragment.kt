package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.navigation.fragment.NavHostFragment
import com.au10tix.poa.PoaFeatureManager
import com.au10tix.poa.PoaResult
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.views.ui.OverlayView
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import java.io.File

class SamplePoaFeatureFragment : BaseFragment() {
    override val requiredPermissions: Array<String?> = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    private lateinit var poaFeatureManager: PoaFeatureManager
    private lateinit var coreManager: Au10xCore
    private lateinit var passableViewGroup: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_open_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passableViewGroup = view.findViewById(R.id.passable_view_group)
        view.findViewById<OverlayView>(R.id.overlay).setFacing(CameraSelector.LENS_FACING_BACK)
        view.findViewById<TextView>(R.id.title).text = "POA"
        view.findViewById<View>(R.id.capture).setOnClickListener { v: View ->
            v.isEnabled = false
            Au10xCore.getInstance(requireContext().applicationContext).captureStillImage()
        }
        poaFeatureManager = PoaFeatureManager(activity, this)
        if (verifyPermissions()) {
            startSession()
        }
    }

    private fun startSession() {
        coreManager = Au10xCore.getInstance(requireContext().applicationContext)
        coreManager.startSession(poaFeatureManager, passableViewGroup, object : SessionCallback {
            override fun onSessionResult(sessionResult: FeatureSessionResult) {
                //The session result contains the result image file, to be later forwarded to the back end.
                var fileToSend: File = sessionResult.imageFile
                viewModel.setProofOfAddressResult((sessionResult as PoaResult))
                NavHostFragment.findNavController(this@SamplePoaFeatureFragment)
                    .navigateUp()
            }

            override fun onSessionError(sessionError: FeatureSessionError) {
                Toast.makeText(context, sessionError.errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onSessionUpdate(captureFrameUpdate: Au10Update) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coreManager.stopSession()
        poaFeatureManager.destroy()

    }
}