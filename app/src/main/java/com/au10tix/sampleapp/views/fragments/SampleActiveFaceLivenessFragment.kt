package com.au10tix.sampleapp.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.navigation.fragment.findNavController
import com.au10tix.activefaceliveness.AFLConsts
import com.au10tix.activefaceliveness.AFLUpdate
import com.au10tix.activefaceliveness.ActiveFaceLivenessFeatureManager
import com.au10tix.faceliveness.FaceLivenessUpdate
import com.au10tix.sampleapp.R
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.core.comm.SessionCallback
import com.au10tix.sdk.protocol.Au10Update
import com.au10tix.sdk.protocol.FeatureSessionError
import com.au10tix.sdk.protocol.FeatureSessionResult

class SampleActiveFaceLivenessFragment : BaseFragment() {

    companion object {
        const val MEDIA_PROJECTION_REQUEST_CODE = 963
    }

    private lateinit var aflFeatureManager: ActiveFaceLivenessFeatureManager
    private lateinit var coreManager: Au10xCore
    private var prePermission = true
    private lateinit var passableViewGroup: FrameLayout
    private lateinit var instructions: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_afl, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passableViewGroup = view.findViewById(R.id.passable_view_group)
        instructions = view.findViewById(R.id.instructions)
        passableViewGroup.doOnLayout {
            coreManager = Au10xCore.getInstance(requireContext())
            aflFeatureManager = ActiveFaceLivenessFeatureManager(requireContext(), this)
            if (verifyPermissions()) {
                view.post {
                    startCore()
                }
            }
        }
    }

    override fun startCore() {
        val fa = aflFeatureManager.checkFeatureAvailability(requireContext())
        if (!fa.isAvailable) {
            Toast.makeText(requireContext(), "${fa.isRequiredFeaturesAvailable} ${fa.isRequiredPermissionsGranted}", Toast.LENGTH_SHORT).show()
        }
        aflFeatureManager.requestScreenRecordingPermission(requireActivity()) {
            if(it) {
                if (aflFeatureManager.canStartSession()) {
                    coreManager.startSession(
                        aflFeatureManager,
                        passableViewGroup,
                        object : SessionCallback {
                            override fun onSessionResult(result: FeatureSessionResult) {
                            }

                            override fun onSessionError(sessionError: FeatureSessionError) {
                                Toast.makeText(context, sessionError.errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            override fun onSessionUpdate(frame: Au10Update) {
                                if (frame is FaceLivenessUpdate) {
                                    handlePflUpdate(frame)
                                } else if (frame is AFLUpdate) {
                                    handleAflUpdate(frame)
                                }
                            }
                        })
                } else {
                    instructions.text = "No more Tries."
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Screen recording permission is mandatory for AFL verification",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun handleAflUpdate(frame: AFLUpdate) {
        if(instructions == null) {
            return
        }
        when (frame.updateType) {
            AFLConsts.AFL_ATTEMPT -> {
                // sent when starting gesture session
            }
            AFLConsts.GESTURE_PASSED -> {
                if (frame.challenge != AFLConsts.GESTURE_CHALLENGE_CENTER)
                    instructions.text = "Well done!"
            }
            AFLConsts.GESTURE_TIMEOUT -> {
                // gesture timeout, moving to the next gesture (will be followed by NEW_GESTURE with the new instructions)
            }
            AFLConsts.GESTURE_TIMEOUT_NO_ATTEMPTS_LEFT -> {
                // gesture timeout - no more tries left. will be followed by result callback or WAITING_FOR_USER with relevant instructions
            }
            AFLConsts.NEW_GESTURE -> {
                // new gesture - need to let the user know what he should do
                when (frame.challenge) {
                    AFLConsts.GESTURE_CHALLENGE_CENTER -> {
                        instructions.text = "Move to center"
                    }
                    AFLConsts.GESTURE_CHALLENGE_LEFT -> {
                        instructions.text = "Turn Face to the left"
                    }
                    AFLConsts.GESTURE_CHALLENGE_RIGHT -> {
                        instructions.text = "Turn Face to the right"
                    }
                }
            }
            AFLConsts.PFL_ATTEMPT -> {
                // sent when starting face capture session
            }
            AFLConsts.RECORDING_STARTED -> {
                // session recording started
            }
            AFLConsts.RECORDING_ENDED -> {
                // session recording ended
            }
            AFLConsts.SELFIE_CAPTURED -> {
                // pfl image captured
            }
            AFLConsts.USER_INTERRUPTED -> {
                // user interrupted to the session. should call AFLFeatureManager.canStartSession()
                // before starting the session again to be sure the user have more tries
                instructions.text = "Session interrupted"
                instructions.postDelayed({ startCore() }, 1500)
            }
            AFLConsts.WAITING_FOR_PFL -> {
                // The session is waiting for pfl validation. result callback will be called when done.
                // consider reflecting the delay to the user with loader.
                instructions.text = "Validating pfl, please wait"
            }
            AFLConsts.WAITING_FOR_USER -> {
                // The session paused to give you an opportunity to show an instructions screen
                // call AFLFeatureManager.proceedToNextStep() to resume the session
                when (frame.waitingReason) {
                    AFLConsts.RETRY_AFL -> {
                        // retrying afl after failure
                        instructions.text = "Retrying AFL"
                    }
                    AFLConsts.RETRY_PFL -> {
                        // retrying pfl after failure
                        instructions.text = "Retrying PFL"
                    }
                    AFLConsts.START_AFL -> {
                        // starting afl for the first time
                        instructions.text = "Starting AFL"
                    }
                    AFLConsts.START_PFL -> {
                        // starting pfl for the first time
                        instructions.text = "Starting PFL"
                    }
                }
                instructions.postDelayed({ aflFeatureManager.proceedToNextStep() }, 1500)
            }
            AFLConsts.WRONG_GESTURE -> {
                // user might turned to the other way (turned left instead of right)
                // consider let him know he did something wrong.
                instructions.text = "Wrong gesture"
            }
        }
    }

    private fun handlePflUpdate(frame: FaceLivenessUpdate) {
        instructions.text = frame.statusDescription
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                prePermission = false
                startCore()
            } else {
                instructions.text = "Permission denied. Cannot start session"
            }
        }
    }
}
