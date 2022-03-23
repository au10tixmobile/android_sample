package com.au10tix.sampleapp.views.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.poa.PoaResult
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.models.DataViewModel
import com.au10tix.sdk.protocol.FeatureSessionResult
import com.au10tix.smartDocument.SmartDocumentResult
import kotlinx.android.synthetic.main.feature_result_dialog.*
import java.util.*

abstract class BaseFragment : Fragment() {
    lateinit var viewModel: DataViewModel

    protected var pDialog: ProgressDialog? = null
    private var alertDialog: AlertDialog? = null

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
        val missingPermissions = ArrayList<String?>()
        for (requiredPermission in requiredPermissions) {
            if (!verifyPermissionGranted(requireContext(), requiredPermission)) {
                missingPermissions.add(requiredPermission)
            }
        }
        if (!missingPermissions.isEmpty()) {
            val missingPermissionArray = missingPermissions.toTypedArray()
            requestPermissions(missingPermissionArray, PERMISSIONS_RQ)
            return false
        }
        return true
    }

    private fun verifyPermissionGranted(context: Context, permission: String?): Boolean {
        val locationPermissionState =
            context.checkPermission(permission!!, Process.myPid(), Process.myUid())
        return locationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_RQ) {
            for (permissionGrantState in grantResults) {
                if (permissionGrantState == PackageManager.PERMISSION_DENIED) {
                    verifyPermissions()
                    return
                }
            }
            startCore()
        }
    }

    protected fun showProgressDialog(shouldShow: Boolean, message: String) {
        if (shouldShow) {
            pDialog = ProgressDialog(context)
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
        var details: String = ""
        lateinit var bitmap: Bitmap
        when (result) {
            is FaceLivenessResult -> {
                val faceLivenessResult = result
                details = """
                 Probability: ${faceLivenessResult.probability}
                 Quality: ${faceLivenessResult.quality}
                 Score: ${faceLivenessResult.score}
                 """.trimIndent()
                bitmap = viewModel.pflResult.value!!.imageRepresentation.bitmap
            }
            is SmartDocumentResult -> {
                val documentResult = result
                details = "ID status: ${documentResult.idStatus}"
                bitmap = documentResult.imageRepresentation.bitmap
            }
            is PoaResult -> {
                val documentResult = result
                bitmap = documentResult.imageRepresentation.bitmap
            }
        }

        val inflater = layoutInflater
        val dialoglayout: View = inflater.inflate(R.layout.feature_result_dialog, null).apply {
            image.setImageBitmap(bitmap)
        }
        alertDialog =
            AlertDialog.Builder(requireContext())
                .setMessage(details)
                .setView(dialoglayout)
                .setPositiveButton("OK") { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
    }

    companion object {
        private const val PERMISSIONS_RQ = 99
    }
}