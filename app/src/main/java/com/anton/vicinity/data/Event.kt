package com.anton.vicinity.data

import java.util.*

data class Event(
    val eventName: String? = null,
    val eventDescription: String? = null,
    val eventLogo: String? = null,
    val eventLat: Double? = null,
    val eventLng: Double? = null,
    val eventTags: MutableList<String>? = null,
    val eventStartDate: String? = null,
    val eventEndDate: String? = null
)