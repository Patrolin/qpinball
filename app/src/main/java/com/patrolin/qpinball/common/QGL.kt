package com.patrolin.qpinball.common

import android.opengl.GLES30
import java.nio.ByteBuffer

// create program
fun glCreateProgram(vertexShader: String, fragmentShader: String): Int {
    val vertexShaderId = glCreateShader(vertexShader, GLES30.GL_VERTEX_SHADER)
    val fragmentShaderId = glCreateShader(fragmentShader, GLES30.GL_FRAGMENT_SHADER)
    val programId = GLES30.glCreateProgram()
    if (programId == 0) throw Exception("Failed to create GL program")
    GLES30.glAttachShader(programId, vertexShaderId)
    GLES30.glAttachShader(programId, fragmentShaderId)
    // link
    GLES30.glLinkProgram(programId)
    val linkStatus = glGetProgramInteger(programId, GLES30.GL_LINK_STATUS)
    if (linkStatus == 0) {
        val programLog = GLES30.glGetProgramInfoLog(programId)
        throw Exception("Failed to link GL program\n\n${programLog}")
    }
    GLES30.glUseProgram(programId)
    return programId
}
fun glCreateShader(src: String, type: Int): Int {
    val shaderId = GLES30.glCreateShader(type)
    if (shaderId == 0) throw Exception("Failed to create shader")
    GLES30.glShaderSource(shaderId, src)
    GLES30.glCompileShader(shaderId)
    val compileStatus = glGetShaderInteger(shaderId, GLES30.GL_COMPILE_STATUS)
    val shaderLog = GLES30.glGetShaderInfoLog(shaderId)
    if ((compileStatus == 0) || shaderLog.startsWith("ERROR")) {
        val errorPosString = "(\\d+):(\\d+)".toRegex().matchAt(shaderLog.removePrefix("ERROR: "), 0)
        val errorPosY = if (errorPosString != null) errorPosString.groupValues[2].toInt() else null
        val errorLine = if (errorPosY != null) "${src.lines()[errorPosY - 1]}\n" else ""
        throw Exception("Failed to compile shader (compileStatus=${compileStatus}):\n\n${src}\n\n${shaderLog}${errorLine}")
    }
    return shaderId
}
fun glGetProgramInteger(programId: Int, id: Int): Int {
    val intArray = IntArray(1)
    GLES30.glGetProgramiv(programId, id, intArray, 0)
    return intArray[0]
}
fun glGetShaderInteger(shaderId: Int, id: Int): Int {
    val intArray = IntArray(1)
    GLES30.glGetShaderiv(shaderId, id, intArray, 0)
    return intArray[0]
}

// buffer
class GLAttribute(
    val id: Int,
    val name: String,
    private val superType: Int,
    private val superCount: Int, // TODO
) {
    override fun toString(): String {
        val typeName = when (type) {
            GLES30.GL_FLOAT -> "GL_FLOAT"
            else -> type.toString()
        }
        return "GLAttribute(id=$id, name=$name, type=$typeName, count=$count, superCount: $superCount)"
    }
    val type: Int get() {
        return when (superType) {
            GLES30.GL_FLOAT, GLES30.GL_FLOAT_VEC2, GLES30.GL_FLOAT_VEC3, GLES30.GL_FLOAT_VEC4,
            GLES30.GL_FLOAT_MAT2, GLES30.GL_FLOAT_MAT3, GLES30.GL_FLOAT_MAT4,
            GLES30.GL_FLOAT_MAT2x3, GLES30.GL_FLOAT_MAT2x4,
            GLES30.GL_FLOAT_MAT3x2, GLES30.GL_FLOAT_MAT3x4,
            GLES30.GL_FLOAT_MAT4x2, GLES30.GL_FLOAT_MAT4x3 -> GLES30.GL_FLOAT
            GLES30.GL_INT, GLES30.GL_INT_VEC2, GLES30.GL_INT_VEC3, GLES30.GL_INT_VEC4 -> GLES30.GL_INT
            GLES30.GL_UNSIGNED_INT, GLES30.GL_UNSIGNED_INT_VEC2, GLES30.GL_UNSIGNED_INT_VEC3, GLES30.GL_UNSIGNED_INT_VEC4 -> GLES30.GL_UNSIGNED_INT
            else -> {
                fail("Unknown superType: $superType")
            }
        }
    }
    val count: Int get() {
        return when (superType) {
            GLES30.GL_FLOAT, GLES30.GL_INT, GLES30.GL_UNSIGNED_INT -> 1
            GLES30.GL_FLOAT_VEC2, GLES30.GL_INT_VEC2, GLES30.GL_UNSIGNED_INT_VEC2 -> 2
            GLES30.GL_FLOAT_VEC3, GLES30.GL_INT_VEC3, GLES30.GL_UNSIGNED_INT_VEC3 -> 3
            GLES30.GL_FLOAT_VEC4, GLES30.GL_INT_VEC4, GLES30.GL_UNSIGNED_INT_VEC4 -> 4
            GLES30.GL_FLOAT_MAT2 -> 4
            GLES30.GL_FLOAT_MAT3 -> 9
            GLES30.GL_FLOAT_MAT4 -> 16
            else -> {
                fail("Unknown superType: $superType")
            }
        }
    }
    val elementSize: Int get() {
        return when (type) {
            GLES30.GL_FLOAT, GLES30.GL_INT, GLES30.GL_UNSIGNED_INT -> 4
            else -> {
                fail("Unknown type: $type")
            }
        }
    }
    val size: Int get() = elementSize * count
}
fun glGetAttributes(programId: Int): List<GLAttribute> {
    val attributeCount = glGetProgramInteger(programId, GLES30.GL_ACTIVE_ATTRIBUTES)
    val maxAttributeLength = glGetProgramInteger(programId, GLES30.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH)
    val attributeLengthBuffer = IntArray(1)
    val attributeNameBuffer = ByteArray(maxAttributeLength)
    val attributeSuperTypeBuffer = IntArray(1)
    val attributeSuperCountBuffer = IntArray(1)
    val acc = arrayListOf<GLAttribute>()
    for (attributeId in 0.until(attributeCount)) {
        GLES30.glGetActiveAttrib(programId, attributeId, maxAttributeLength, attributeLengthBuffer, 0,
            attributeSuperCountBuffer, 0, attributeSuperTypeBuffer, 0, attributeNameBuffer, 0)
        val attributeLength = attributeLengthBuffer[0]
        val attributeName = attributeNameBuffer.sliceArray(0.until(attributeLength)).toString(Charsets.UTF_8)
        val attributeSuperType = attributeSuperTypeBuffer[0]
        val attributeSuperCount = attributeSuperCountBuffer[0]
        val attribute = GLAttribute(attributeId, attributeName, attributeSuperType, attributeSuperCount)
        // TODO: skip attributeIds if matrix?
        acc.add(attribute)
    }
    return acc
}
fun glSetupBuffer(programId: Int, attributeNames: List<String>): Pair<Int, Int> {
    val bufferIdsBuffer = IntArray(1)
    GLES30.glGenBuffers(1, bufferIdsBuffer, 0)
    val bufferId = bufferIdsBuffer[0]
    val attributeMap = glGetAttributes(programId).associateBy { it.name }
    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferId)
    var vertexSize = 0
    for (attributeName in attributeNames) {
        val attribute = attributeMap[attributeName]
        debugPrint("attribute: $attribute")
        vertexSize += attribute!!.size
    }
    var offset = 0
    for (attributeName in attributeNames) {
        val attribute = attributeMap[attributeName]
        GLES30.glEnableVertexAttribArray(attribute!!.id)
        val count = attribute.count
        val countDivisor = count / 4
        val countRemainder = count % 4
        for (i in 0.until(countDivisor)) {
            GLES30.glVertexAttribPointer(attribute.id + i, 4, attribute.type, false, vertexSize, offset)
        }
        if (countRemainder != 0) {
            GLES30.glVertexAttribPointer(attribute.id + countDivisor, countRemainder, attribute.type, false, vertexSize, offset)
        }
        offset += attribute.size
    }
    return bufferId to vertexSize
}

// write data
fun glWriteInt(buffer: ByteBuffer, value: Int): Int {
    buffer.put(value.shr(24).toByte())
    buffer.put(value.shr(16).toByte())
    buffer.put(value.shr(8).toByte())
    buffer.put(value.toByte())
    return 4
}
fun glWriteFloat(buffer: ByteBuffer, value: Float): Int {
    val bits = value.toBits()
    glWriteInt(buffer, reverseBytes(bits))
    return 4
}