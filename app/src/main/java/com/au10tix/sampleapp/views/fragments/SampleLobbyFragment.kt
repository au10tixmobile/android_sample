package com.au10tix.sampleapp.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.au10tix.faceliveness.FaceLivenessFeatureManager
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.poa.PoaFeatureManager
import com.au10tix.poa.PoaResult
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.helpers.Au10NetworkHelper
import com.au10tix.sampleapp.models.DataViewModel
import com.au10tix.sdk.abstractions.FeatureManager
import com.au10tix.sdk.commons.Au10Error
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.OnPrepareCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.sdk.ui.Au10UIManager
import com.au10tix.sdk.ui.UICallback
import com.au10tix.smartDocument.SmartDocumentFeatureManager
import com.au10tix.smartDocument.SmartDocumentResult
import kotlinx.android.synthetic.main.fragment_lobby_sample.*

class SampleLobbyFragment : BaseFragment() {

    private lateinit var au10UIManager: Au10UIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ViewModelProvider(requireActivity()).get(DataViewModel::class.java).jwtToken.value.isNullOrEmpty()) {

            showProgressDialog(true, "Preparing Au10xCore")
            //Retrieve a jwt bearer token by sending the session's scope (Usually this is "mobilesdk pfl sdc") to the designated endpoint

            Au10NetworkHelper.getBearerToken {
                if (it == null) {
                    Log.d("getBearerToken", "Token not acquired")
                    return@getBearerToken
                }
                try {
                    //To run detection sessions, first prepare Au10xCore by passing the jwt token and an onPrepare callback.
                    Au10xCore.prepare(
                        context,
                        it,
                        object : OnPrepareCallback {
                            override fun onPrepareError(error: Au10Error) {
                                Log.d("prepare", "onPrepareError")
                                Toast.makeText(context, "Session prepare failed", Toast.LENGTH_LONG)
                                    .show()
                                showProgressDialog(false, "")
                            }

                            override fun onPrepared(sessionId: String) {
                                //Only once Au10xCore is prepared it can perform detection sessions.
                                Log.d("prepare", "onPrepared")
                                ViewModelProvider(requireActivity()).get(DataViewModel::class.java).sessionId =
                                    sessionId
                                Toast.makeText(context, "Session prepared", Toast.LENGTH_LONG)
                                    .show()
                                showProgressDialog(false, "")
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Session prepare failed",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startFeatureEvaluationButton.setOnClickListener {
            val bundle = Bundle()

            when (resources.getStringArray(R.array.features)[featuresSpinner.selectedItemPosition]) {
                "Passive Face Liveness" -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_lobbyFragment_to_faceLivenessFragment, bundle)
                "Smart Document Capture" -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_lobbyFragment_to_smartDocumentFragment, bundle)
                "Proof of Address" -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_lobby_to_POA)
                "SDC UI" -> {
                    val smartDocumentFeatureManager = SmartDocumentFeatureManager(context, this)
                    handleSdkUI(smartDocumentFeatureManager)
                }
                "PFL UI" -> {
                    val faceLivenessFeatureManager = FaceLivenessFeatureManager(context, this)
                    handleSdkUI(faceLivenessFeatureManager)
                }
                "POA UI" -> {
                    handleSdkUI(PoaFeatureManager(context, this))
                }
                "Active Face Liveness" -> NavHostFragment.findNavController(this)
                    .navigate(R.id.action_lobby_to_afl)
            }
        }

        backend.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_lobby_to_Backend)
        }

        if (viewModel.sdcFrontResult.value != null) {
            id_front_iv.setImageBitmap(viewModel.sdcFrontResult.value!!.imageRepresentation.bitmap)

            id_front_iv.setOnClickListener {
                val result: FeatureSessionResult? = viewModel.sdcFrontResult.value
                if (result != null) {
                    displayFeatureResultDialogBox(result)
                }
            }
        }

        if (viewModel.pflResult.value != null) {
            face_photo_iv.setImageBitmap(viewModel.pflResult.value!!.imageRepresentation.bitmap)

            face_photo_iv.setOnClickListener {
                val result: FeatureSessionResult? = viewModel.pflResult.value
                if (result != null) {
                    displayFeatureResultDialogBox(result)
                }
            }
        }
        if (viewModel.poaResult.value != null) {
            poa_iv.setImageBitmap(viewModel.poaResult.value!!.imageRepresentation.bitmap)

            poa_iv.setOnClickListener {
                val result: FeatureSessionResult? = viewModel.poaResult.value
                if (result != null) {
                    displayFeatureResultDialogBox(result)
                }
            }
        }
    }

    private fun handleSdkUI(featureManager: FeatureManager) {
        val nav = NavHostFragment.findNavController(this)
        //To use the default user interface, instantiate a FeatureManager, and pass it to the Au10UIManager as follows.

        val builder = Au10UIManager.Builder(activity, featureManager, object : UICallback() {
            //Set a UICallback to handle the received session results, errors, updates, and failures.

            override fun onSessionResult(sessionResult: FeatureSessionResult) {
                when (sessionResult) {
                    is FaceLivenessResult -> {
                        viewModel.setFaceLivenessResult(sessionResult)
                    }
                    is SmartDocumentResult -> {
                        viewModel.setSmartDocumentResult(sessionResult)
                    }
                    is PoaResult -> {
                        viewModel.setProofOfAddressResult(sessionResult)
                    }
                }
                nav.navigateUp()
                featureManager.destroy()
            }

            override fun onSessionError(sessionError: FeatureSessionError) {
                Toast.makeText(
                    context,
                    "SDK UI received error: " + sessionError.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("debug app", "error: " + sessionError.errorMessage)
                nav.navigateUp()
            }

            override fun onSessionUpdate(captureFrameUpdate: Au10Update) {}
            override fun onFail(result: FeatureSessionResult) {}
        })
        //The UI provided along the SDK can be styled as follows:
        au10UIManager = builder.build()
        val fragment: Fragment? = au10UIManager.generateFragment()
        if (fragment != null) {
            NavHostFragment.findNavController(this)
                .navigate(R.id.start_au10Fragment, fragment.arguments)
        } else {
            Toast.makeText(context, "Missing permissions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_lobby_sample, container, false)
    }
}
