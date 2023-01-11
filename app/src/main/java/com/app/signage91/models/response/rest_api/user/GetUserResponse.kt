package com.app.signage91.models.response.rest_api.user

import com.google.gson.annotations.SerializedName

data class GetUserResponse(

    @field:SerializedName("GetUserResponse")
    val getUserResponse: List<GetUserResponseItem?>? = null
)

data class GetUserResponseItem(

    @field:SerializedName("balance")
    val balance: String? = null,

    @field:SerializedName("gender")
    val gender: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("company")
    val company: String? = null,

    @field:SerializedName("picture")
    val picture: String? = null,

    @field:SerializedName("age")
    val age: Int? = null,

    @field:SerializedName("email")
    val email: String? = null
)
