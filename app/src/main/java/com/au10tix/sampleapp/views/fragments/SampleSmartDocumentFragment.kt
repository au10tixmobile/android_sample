package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.navigation.fragment.NavHostFragment
import com.au10tix.sampleapp.R
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.smartDocument.SmartDocumentFeatureManager
import com.au10tix.smartDocument.SmartDocumentFeatureSessionFrame
import com.au10tix.smartDocument.SmartDocumentResult
import kotlinx.android.synthetic.main.fragment_open_camera.*
import java.io.File

class SampleSmartDocumentFragment : BaseFragment() {
    override val requiredPermissions: Array<String?> = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    private lateinit var coreManager: Au10xCore
    private lateinit var smartDocumentFeatureManager: SmartDocumentFeatureManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_open_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coreManager = Au10xCore.getInstance(requireContext().applicationContext)
        val title = requireArguments().getString(TITLE_KEY)
        overlay.setFacing(CameraSelector.LENS_FACING_BACK)
        (view.findViewById<View>(R.id.title) as TextView).text = title
        capture.setOnClickListener { v: View ->
            v.isEnabled = false
            coreManager.captureStillImage()
        }
        //instantiate a Smart Document Feature Manager
        smartDocumentFeatureManager = SmartDocumentFeatureManager(activity, this)
        if (verifyPermissions()) {
            startCore()
        }
    }

    override fun startCore() {
        //Start the session by passing the feature manager, the view group, and a SessionCallback that will host the UI, to the prepared Au10xCore instance.
        coreManager.startSession(smartDocumentFeatureManager,
            passable_view_group,
            object : SessionCallback {
                override fun onSessionResult(sessionResult: FeatureSessionResult) {
                    //The session result contains the result image file, to be later forwarded to the back end.
                    var fileToSend: File = sessionResult.imageFile
                    viewModel.setSmartDocumentResult((sessionResult as SmartDocumentResult))
                    NavHostFragment.findNavController(this@SampleSmartDocumentFragment)
                        .navigateUp()
                }

                override fun onSessionError(sessionError: FeatureSessionError) {
                    Toast.makeText(context, sessionError.errorMessage, Toast.LENGTH_SHORT).show()
                    NavHostFragment.findNavController(this@SampleSmartDocumentFragment)
                        .navigateUp()
                }

                override fun onSessionUpdate(captureFrameUpdate: Au10Update) {
                    //The Au10Update can now be cast to SmartDocumentFeatureSessionFrame. The results are accessed as follows:
                    details.visibility = View.VISIBLE
                    val frame = captureFrameUpdate as SmartDocumentFeatureSessionFrame
                    val sb = StringBuilder()
                        .append("id status: ").append(frame.idStatus)
                    details.text = sb.toString()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coreManager.stopSession()
        smartDocumentFeatureManager.destroy()
    }

    companion object {
        protected const val TITLE_KEY = "title"
    }
}