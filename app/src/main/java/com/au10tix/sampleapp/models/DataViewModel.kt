package com.au10tix.sampleapp.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.au10tix.faceliveness.FaceLivenessResult
import com.au10tix.poa.PoaResult
import com.au10tix.smartDocument.SmartDocumentResult

class DataViewModel : ViewModel() {

    val jwtToken = MutableLiveData<String?>()
    val sdcFrontResult = MutableLiveData<SmartDocumentResult>()
    val pflResult = MutableLiveData<FaceLivenessResult>()
    val poaResult = MutableLiveData<PoaResult>()
    var sessionId: String? = null

    fun setJwtToken(token: String?) {
        jwtToken.postValue(token)
    }

    fun setFaceLivenessResult(pflSessionResult: FaceLivenessResult) {
        pflResult.postValue(pflSessionResult)
    }

    fun setSmartDocumentResult(sessionResult: SmartDocumentResult) {
        sdcFrontResult.postValue(sessionResult)
    }

    fun setProofOfAddressResult(sessionResult: PoaResult) {
        poaResult.value = sessionResult
    }
}