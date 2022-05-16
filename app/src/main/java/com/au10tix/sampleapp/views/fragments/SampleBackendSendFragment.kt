package com.au10tix.sampleapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.au10tix.backend.Au10Backend
import com.au10tix.backend.BackendCallback
import com.au10tix.backend.PersonalDetails
import com.au10tix.sampleapp.R
import com.au10tix.sdk.core.Au10xCore

class SampleBackendSendFragment : BaseFragment() {

    private lateinit var description: TextView
    private val callback = object : BackendCallback {
        override fun onSuccess(requestID: String?) {
            description.text = "Request id:\n$requestID"
        }

        override fun onError(error: String?) {
            description.text = "sending data encountered error:\n$error"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_backend_sender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        description = view.findViewById(R.id.description)
        view.findViewById<Button>(R.id.send).setOnClickListener {
            if(!Au10xCore.isPrepared()) {
                Toast.makeText(context, "SDK not prepared", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            description.text = "sending data"
            val spinner = view.findViewById<Spinner>(R.id.backendTypeSpinner)
            when (resources.getStringArray(R.array.backend)[spinner.selectedItemPosition]) {
                "ID verification" -> performIDV()
                "Proof of Address" -> performPOA()
            }
        }
    }

    private fun performIDV() {
        Au10Backend.sendIDVerification(callback)
    }

    private fun performPOA() {
        //todo: Use real user data here
        val personalDetails = PersonalDetails("firstName", "lastName", "address")
        Au10Backend.sendProofOfAddress(personalDetails, callback)
    }
}