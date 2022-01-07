package io.nozemi.discordnews.helpers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

fun xmlMapper(): ObjectMapper = ObjectMapper(XmlFactory())
    .findAndRegisterModules()
    .registerKotlinModule()
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    .enable(SerializationFeature.INDENT_OUTPUT)

fun yamlMapper(): ObjectMapper = ObjectMapper(YAMLFactory())
    .findAndRegisterModules()
    .registerKotlinModule()
    .enable(SerializationFeature.INDENT_OUTPUT)