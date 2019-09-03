package com.example.basearch.presentation.ui

import androidx.lifecycle.ViewModel
import br.com.pebmed.domain.base.ResultWrapper

open class BaseViewModel : ViewModel() {
    fun <SUCCESS, ERROR> handleDefaultViewStateResourceResult(resultWrapper: ResultWrapper<SUCCESS?, ERROR>): ViewStateResource<SUCCESS> {


        val viewStateResource = when (resultWrapper) {
            is ResultWrapper.Success -> {
                ViewStateResource.Success(resultWrapper.data)
            }

            is ResultWrapper.Error -> {
                ViewStateResource.Error(resultWrapper.data as String)
            }
        }
        return viewStateResource
    }
}