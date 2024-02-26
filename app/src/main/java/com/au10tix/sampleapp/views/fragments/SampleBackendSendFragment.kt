package com.au10tix.sampleapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.au10tix.sampleapp.R
import com.au10tix.sdk.backend.Au10Backend
import com.au10tix.sdk.backend.BackendCallback
import com.au10tix.sdk.core.Au10xCore
import com.au10tix.sdk.protocol.FeatureSessionError

class SampleBackendSendFragment : BaseFragment() {

    private lateinit var description: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_backend_sender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        description = view.findViewById(R.id.description)
        view.findViewById<Button>(R.id.send).setOnClickListener {
            if (!Au10xCore.isPrepared()) {
                Toast.makeText(context, "SDK not prepared", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            description.text = "sending data"
            Au10Backend.sendRequest(object : BackendCallback {
                override fun onSuccess(requestID: String?) {
                    description.text = "Request id:\n$requestID"
                }

                override fun onError(error: FeatureSessionError?) {
                    description.text = "sending data encountered error:\n${error?.description}"

                }
            })
        }
    }
}
