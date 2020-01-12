package blogify.backend.config

import blogify.backend.util.env

import com.typesafe.config.ConfigFactory

import kotlinx.serialization.DeserializationStrategy

import org.jetbrains.kotlinx.serialization.config.ConfigParser

import java.io.File

private val directory = env("BLOGIFY_CONFIG_DIRECTORY") ?: error("BLOGIFY_CONFIG_DIRECTORY is not set - cannot load config - cannot start application")

private val mainConfig = ConfigFactory.parseFile(File("$directory/blogify.conf"))

fun <T : Any> loadConfig(name: String, deserializer: DeserializationStrategy<T>)
        = ConfigParser.parse(mainConfig.getConfig(name), deserializer)