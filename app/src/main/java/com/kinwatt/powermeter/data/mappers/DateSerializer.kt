package com.kinwatt.powermeter.data.mappers

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("DateSerializer")

    override fun serialize(output: Encoder, obj: Date) {
        output.encodeString(obj.time.toString())
    }

    override fun deserialize(input: Decoder): Date {
        return Date(input.decodeString().toLong())
    }
}