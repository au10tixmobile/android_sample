package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.au10tix.faceliveness.FaceLivenessFeatureManager
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.faceliveness.FaceLivenessUpdate
import com.au10tix.faceliveness.LivenessCallback
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.models.DataViewModel
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import java.io.File

class SampleFaceLivenessFragment : BaseFragment() {
    override val requiredPermissions: Array<String?> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var previewParentView: FrameLayout? = null
    private lateinit var coreManager: Au10xCore
    private lateinit var faceLiveness: FaceLivenessFeatureManager
    private lateinit var details: TextView
    private lateinit var preview: ImageView
    private lateinit var capture: Button
    private lateinit var recapture: Button
    private lateinit var validate: Button
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_passive_face_liveness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        details = view.findViewById(R.id.details)
        preview = view.findViewById(R.id.preview)
        capture = view.findViewById(R.id.capture)
        recapture = view.findViewById(R.id.recapture)
        validate = view.findViewById(R.id.validate)
        title = view.findViewById(R.id.title)

        viewModel = ViewModelProvider(requireActivity())[DataViewModel::class.java]
        previewParentView = view.findViewById(R.id.passable_view_group)
        title.text = requireArguments().getString(TITLE_KEY)
        capture.setOnClickListener { coreManager.captureStillImage() }
        coreManager = Au10xCore.getInstance(requireContext().applicationContext)
        recapture.setOnClickListener {
            hidePreview()
            retry()
        }
        validate.setOnClickListener {
            showProgressDialog(true, "Validating")

            faceLiveness.validateLiveness(
                //Validate face liveness by passing the received pfl result and a LivenessCallback to validateLiveness()
                viewModel.pflResult.value, object : LivenessCallback {
                    override fun onSuccess(result: FaceLivenessResult) {
                        showProgressDialog(false, "Validating")
                        //Handle the result parameters according to your business logic.
                        details.text = String.format(
                            "Probability: %s\nQuality: %s\nScore: %s",
                            result.probability,
                            result.quality,
                            result.score
                        )
                        viewModel.setFaceLivenessResult(result)
                        NavHostFragment.findNavController(this@SampleFaceLivenessFragment)
                            .navigateUp()
                    }

                    override fun onFail(result: FaceLivenessResult) {
                        showProgressDialog(false, "Validating")

                        details.text = String.format(
                            "Probability: %s\nQuality: %s\nScore: %s",
                            result.probability,
                            result.quality,
                            result.score
                        )
                        details.visibility = View.VISIBLE
                        viewModel.setFaceLivenessResult(result)
                        NavHostFragment.findNavController(this@SampleFaceLivenessFragment)
                            .navigateUp()
                    }

                    override fun onError(error: FeatureSessionError) {
                        showProgressDialog(false, "Validating")

                        details.text = String.format("%s", error.errorMessage)
                        details.visibility = View.VISIBLE
                        Toast.makeText(context, error.errorMessage, Toast.LENGTH_SHORT).show()
                        NavHostFragment.findNavController(this@SampleFaceLivenessFragment)
                            .navigateUp()
                    }
                })
        }
        //Instantiate a FaceLiveness Feature Manager
        faceLiveness = FaceLivenessFeatureManager(activity, this)
        if (verifyPermissions()) {
            faceLiveness.verifyFeatureRequirementsFulfilled(requireActivity()) {
                if (it) {
                    startCore()
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun retry() {
        hidePreview()
        if (faceLiveness.canRetry()) {
            startCore()
        } else {
            Toast.makeText(context, "max retries", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hidePreview() {
        preview.visibility = View.GONE
        capture.visibility = View.VISIBLE
        recapture.visibility = View.GONE
        validate.visibility = View.GONE
    }

    override fun startCore() {
        //Start the session by passing the feature manager, the view group, and a SessionCallback that will host the UI, to the prepared Au10xCore instance.
        coreManager.startSession(faceLiveness, previewParentView, object : SessionCallback {
            override fun onSessionResult(sessionResult: FeatureSessionResult) {
                //The session result contains the result image file, to be later forwarded to the back end.
                var fileToSend: File = sessionResult.imageFile
                capture.visibility = View.GONE
                preview.visibility = View.VISIBLE
                recapture.visibility = View.VISIBLE
                validate.visibility = View.VISIBLE
                val bitmap = sessionResult.frameData.bitmap
                preview.setImageBitmap(bitmap)
                viewModel.setFaceLivenessResult(sessionResult as FaceLivenessResult)
                //The quad object under frame data represents the face's borders, signifying its location
                val quad = sessionResult.frameData.quad
            }

            override fun onSessionError(sessionError: FeatureSessionError) {
                Toast.makeText(context, sessionError.errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onSessionUpdate(captureFrameUpdate: Au10Update) {
                //The Au10Update statusDescription field holds the result of the last frame sent for evaluation.
                if (captureFrameUpdate is FaceLivenessUpdate) {
                    title.text = captureFrameUpdate.statusDescription
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::coreManager.isInitialized) {
            coreManager.stopSession()
        }
        if (::faceLiveness.isInitialized) {
            faceLiveness.destroy()
        }
        previewParentView = null
    }

    companion object {
        private const val TITLE_KEY = "title"
    }
}