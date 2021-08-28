package com.magicsweet.pluginupdater

import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.Console
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry

fun main(args: Array<String>) {
	
	if (args.isEmpty()) {
		print(
			"""
				
				Usage of PluginUpdater:
				
				./plugin-updater.jar
					<target plugin,target plugin 2 ...>
					<root server directory, default: ..>
					<local jar file, default: first found target plugin in ./>
					<backup old plugin version, default: true>
				example: ./plugin-updater.jar ViaVersion .. ./ViaVersion.jar
				
				tip: you can use "<argument>"
				
			""".trimIndent()
		)
		return
	}
	
	for (plugin in args[0].split(",")) {
		args[0] = plugin
		run(args)
	}
	println("(info) execution completed. quitting")
}

fun run(args: Array<String>) {
	println("\n(info) updating ${args[0]}")
	PluginUpdater.initialize(
		args[0],
		if (args.size <= 1) File("..") else File(args[1]),
		if (args.size <= 2)
			File(".").listFiles()?.
			filter { it.name.endsWith(".jar") }?.filter {
				try {
					InputStreamReader(
						JarFile(it).getInputStream(
							ZipEntry("plugin.yml")
						)
					)
					return@filter true
				} catch (e: Exception) {
					return@filter false
				}
			}?.filter {
				PluginUpdater.isFileIsPlugin(it, args[0])
			}
				?.getOrNull(0)
				?: run {
					println("(error) No jar file containing plugin ${args[0]} was found!")
					return
				} else {
			if (File(args[2]).exists()) {
				File(args[2])
			} else {
				println("(error) No such file: ${args[2]}")
				return
			}
		},
		if (args.size <= 3) true else args[3].lowercase().toBooleanStrictOrNull() ?: run {
			println("(error) Expected boolean, got: ${args[3]}")
			return
		}
	)
	
}