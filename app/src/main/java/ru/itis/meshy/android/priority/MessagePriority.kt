package ru.itis.meshy.android.priority

enum class MessagePriority(val value: Byte) {
    STANDARD(0),
    EMERGENCY(1);

    companion object {
        fun fromByte(b: Byte): MessagePriority = entries.first { it.value == b }
    }
}