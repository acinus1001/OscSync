package dev.kuro9.internal.smartapp.model

import kotlinx.serialization.SerialName

enum class Permission {
    @SerialName("r")
    R,
    @SerialName("w")
    W,
    @SerialName("x")
    X;
}