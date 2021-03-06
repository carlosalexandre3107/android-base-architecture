package com.example.basearch.data.remote

import com.example.basearch.data.ResultWrapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

class ApiResponseHandler {
    inline fun <SUCCESS, reified ERROR> handleApiResponse(response: Response<SUCCESS>): ResultWrapper<SUCCESS, BaseErrorData<ERROR>> {
        val headers = response.headers()

        val getHeadersHashMap = {
            val keyValueMap: MutableMap<String, String> = HashMap()

            headers.names().map { headerKey ->
                val headerValue = headers.get(headerKey)
                keyValueMap[headerKey] = headerValue ?: ""
            }

            keyValueMap
        }

        if (response.isSuccessful) {
            //TODO force to crash if body is null?
            val body = response.body()
            return if(body != null)
                ResultWrapper.Success(body, getHeadersHashMap(), StatusType.getByCode(response.code()))
            else
                ResultWrapper.Error(keyValueMap = getHeadersHashMap(), statusCode = StatusType.NULL_BODY_EXCEPTION)
        } else {
            var errorData: ERROR? = null

            when (ERROR::class) {
                Void::class -> {
                }
                else -> {
                    val msg = response.errorBody()?.string()

                    if (!msg.isNullOrEmpty()) {
                        errorData = Gson().fromJsonGeneric<ERROR>(msg)
                    }
                }
            }

            val remoteErrorData = BaseErrorData(
                errorData,
                response.message()
            )

            return ResultWrapper.Error(remoteErrorData, getHeadersHashMap(), StatusType.getByCode(response.code()))
        }
    }

    inline fun <reified T> Gson.fromJsonGeneric(json: String): T =
        this.fromJson<T>(json, object : TypeToken<T>() {}.type)
}