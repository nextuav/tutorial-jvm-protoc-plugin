package com.urbanmatrix.tutorial.protoc.plugin.plugin

import com.google.protobuf.Descriptors
import com.google.protobuf.compiler.PluginProtos
import com.urbanmatrix.tutorial.protoc.plugin.api.CustomInterface

const val PROTO_PACKAGE = "urbanmatrix.tutorial"
var id = 0

fun main() {
    val generatorRequest = PluginProtos.CodeGeneratorRequest.parseFrom(System.`in`)
    val generatorResponse = PluginProtos.CodeGeneratorResponse.newBuilder().apply {
        supportedFeatures = PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE.toLong()
    }

    generatorRequest.protoFileList
        .filter { it.`package`.startsWith(PROTO_PACKAGE) }
        .map { Descriptors.FileDescriptor.buildFrom(it, emptyArray()) }
        .forEach { fileDescriptor ->
            fileDescriptor.messageTypes.forEach { descriptor ->
                val javaFileName = fileDescriptor.options.javaPackage.replace(".", "/") +
                    "/" + descriptor.name.substringAfterLast(".") + ".java"

                generatorResponse.addFileBuilder().apply {
                    name = javaFileName
                    content = CustomInterface::class.java.name + ","
                    insertionPoint = "message_implements:${descriptor.fullName}"
                }

                generatorResponse.addFileBuilder().apply {
                    name = javaFileName
                    content = generateIdCode()
                    insertionPoint = "class_scope:${descriptor.fullName}"
                }
            }
        }

    generatorResponse.build().writeTo(System.out)
}

fun generateIdCode(): String {
    return """
        |private static final int ID = ${++id};
        |
        |@${Override::class.java.name}
        |public int getId() {
        |  return ID;
        |}
    """.trimMargin()
}
