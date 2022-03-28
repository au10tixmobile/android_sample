package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
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
import kotlinx.android.synthetic.main.fragment_passive_face_liveness.*
import java.io.File
import kotlin.math.max
import kotlin.math.min

class SampleFaceLivenessFragment : BaseFragment() {
    override val requiredPermissions: Array<String?> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var previewParentView: FrameLayout? = null
    private lateinit var coreManager: Au10xCore
    private lateinit var faceLiveness: FaceLivenessFeatureManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_passive_face_liveness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(DataViewModel::class.java)
        previewParentView = view.findViewById(R.id.passable_view_group)
        overlay.setFacing(CameraSelector.LENS_FACING_FRONT)
        (view.findViewById<View>(R.id.title) as TextView).text =
            requireArguments().getString(TITLE_KEY)
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
        if (faceLiveness.retry()) {
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
        overlay.setQuad(null)
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
                val min = min(bitmap.width, bitmap.height)
                val max = max(bitmap.width, bitmap.height)
                overlay.setCameraInfo(min, max)
                //The quad object under frame data represents the face's borders, signifying its location
                val quad = sessionResult.frameData.quad
                if (quad != null) {
                    overlay.setQuad(quad)
                }
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (preview != null && preview.visibility == View.VISIBLE) {
            preview.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int,
                ) {
                    v.removeOnLayoutChangeListener(this)
                    preview.post {
                        if (viewModel != null) {
                            val result = viewModel.pflResult.value
                            if (result != null) {
                                val bitmap = result.frameData.bitmap
                                val actualHeight: Int
                                val actualWidth: Int
                                val imageViewHeight = preview.height
                                val imageViewWidth = preview.width
                                val bitmapHeight = bitmap.height
                                val bitmapWidth = bitmap.width
                                if (imageViewHeight * bitmapWidth <= imageViewWidth * bitmapHeight) {
                                    actualWidth = bitmapWidth * imageViewHeight / bitmapHeight
                                    actualHeight = imageViewHeight
                                } else {
                                    actualHeight = bitmapHeight * imageViewWidth / bitmapWidth
                                    actualWidth = imageViewWidth
                                }
                                val min = min(bitmap.width, bitmap.height)
                                val max = max(bitmap.width, bitmap.height)
                                if (actualWidth < actualHeight) {
                                    overlay.setCameraInfo(min, max)
                                } else {
                                    overlay.setCameraInfo(max, min)
                                }
                                val layoutParams = overlay.layoutParams
                                layoutParams.width = actualWidth
                                layoutParams.height = actualHeight
                                overlay.layoutParams = layoutParams
                                overlay.invalidate()
                            }
                        }
                    }
                }
            })
        }
    }

    companion object {
        private const val TITLE_KEY = "title"
    }
}