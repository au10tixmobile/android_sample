package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.poa.PoaResult
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.models.DataViewModel
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.smartDocument.SmartDocumentResult

abstract class BaseFragment : Fragment() {
    lateinit var viewModel: DataViewModel

    protected var pDialog: ProgressDialog? = null
    private var alertDialog: AlertDialog? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            for (key: String in permissions.keys) {
                if (!permissions[key]!!) {
                    verifyPermissions()
                    return@registerForActivityResult
                }
            }
            startCore()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(
            DataViewModel::class.java
        )
    }

    protected open val requiredPermissions: Array<String?>
        get() = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

    protected open fun startCore() {}

    protected fun verifyPermissions(): Boolean {
        val missingPermissions = ArrayList<String>()
        for (requiredPermission in requiredPermissions) {
            if (!verifyPermissionGranted(requireContext(), requiredPermission)) {
                requiredPermission.let { missingPermissions.add(it!!) }
            }
        }
        if (missingPermissions.isNotEmpty()) {
            val missingPermissionArray = missingPermissions.toTypedArray()
            requestPermissionLauncher.launch(missingPermissionArray)
            return false
        }
        return true
    }

    private fun verifyPermissionGranted(context: Context, permission: String?): Boolean {
        val locationPermissionState =
            context.checkPermission(permission!!, Process.myPid(), Process.myUid())
        return locationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    protected fun showProgressDialog(shouldShow: Boolean, message: String) {
        if (shouldShow) {
            pDialog = ProgressDialog(context,0)
            pDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            pDialog!!.setMessage(message)
            pDialog!!.isIndeterminate = false
            pDialog!!.setCancelable(false)
            pDialog!!.setCanceledOnTouchOutside(false)
            pDialog!!.show()
        } else {
            if (pDialog != null && pDialog!!.isShowing) {
                pDialog!!.dismiss()
                pDialog = null
            }
        }
    }

    protected fun displayFeatureResultDialogBox(result: FeatureSessionResult?) {
        if (result == null) {
            return
        }
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        var details = ""
        lateinit var bitmap: Bitmap
        when (result) {
            is FaceLivenessResult -> {
                details = """
                 Probability: ${result.probability}
                 Quality: ${result.quality}
                 Score: ${result.score}
                 """.trimIndent()
                bitmap = viewModel.pflResult.value!!.imageRepresentation.bitmap
            }
            is SmartDocumentResult -> {
                details = "ID status: ${result.idStatus}"
                bitmap = result.imageRepresentation.bitmap
            }
            is PoaResult -> {
                bitmap = result.imageRepresentation.bitmap
            }
        }

        val inflater = layoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.feature_result_dialog, null).apply {
            findViewById<ImageView>(R.id.image).setImageBitmap(bitmap)
        }
        alertDialog =
            AlertDialog.Builder(requireContext())
                .setMessage(details)
                .setView(dialogLayout)
                .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
    }

    companion object {
        private const val PERMISSIONS_RQ = 99
    }
}
