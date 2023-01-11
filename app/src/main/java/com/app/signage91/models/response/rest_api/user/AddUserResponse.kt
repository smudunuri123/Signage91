package com.app.signage91.models.response.rest_api.user

import com.google.gson.annotations.SerializedName

data class AddUserResponse(

	@field:SerializedName("data")
	val data: String? = null,

	@field:SerializedName("errors")
	val errors: List<Any?>? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)
