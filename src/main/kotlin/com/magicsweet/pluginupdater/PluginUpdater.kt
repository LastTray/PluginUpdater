package com.magicsweet.pluginupdater

import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import java.util.stream.Collectors
import java.util.zip.ZipEntry


class PluginUpdater
	constructor(
		private val targetPlugin: String,
		private val rootDirectory: File,
		private val jar: File,
		private val backupOld: Boolean
	)
{
	companion object {
		private lateinit var instance: PluginUpdater
		private var auto = false
		
		
		fun initialize(targetPlugin: String, rootDirectory: File, jar: File, backupOld: Boolean) {
			PluginUpdater(targetPlugin, rootDirectory, jar, backupOld)
		}
		
		fun isFileIsPlugin(it: File, plugin: String = instance.targetPlugin): Boolean {
			return try {
				YamlConfigurationLoader.builder().source {
					BufferedReader(
						InputStreamReader(
							JarFile(it).getInputStream(
								ZipEntry("plugin.yml")
							)
						)
					)
				}.build().load().node("name").string == plugin
			} catch (e: Exception) {
				false
			}
		}
		
	}
	
	private val time = System.currentTimeMillis()
	
	init {
		instance = this
		println(
			"""
				
				starting updater...
				
				update info:
				- Target plugin: $targetPlugin
				- Root server directory: ${rootDirectory.canonicalPath}
				- Jar file for update: ${jar.canonicalPath}
				- Backup old version? $backupOld
				
			""".trimIndent()
		)
		
		var input: String
		
		if (auto) update()
		while (!auto) {
			print("continue? [y/n/a] ")
			input = readLine()?.lowercase() ?: ""
			
			if ("all".startsWith(input)) {
				auto = true
				update()
				break
			}
			
			if ("yes".startsWith(input) || input == "") {
				update()
				break
			} else {
				if ("no".startsWith(input)) {
					println("(info) quit")
					break
				}
			}
		}
		
	}
	
	private fun update() {
		println("(worker) discovering available plugins to update...")
		
		var walker = Files.walk(rootDirectory.toPath()).collect(Collectors.toList()).map(Path::toFile)
		
		var bar = ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setTaskName("filtering files").setInitialMax(walker.size.toLong()).build()
		
		walker = walker
		.filter {
			bar.step()
			it.isFile
		}
		bar.close()
		bar = ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setTaskName("discovering jars").setInitialMax(walker.size.toLong() - 1).build()
		walker = walker
			.filter {
				it.canonicalPath != jar.canonicalPath
			}
			.filter {
				bar.step()
				isFileIsPlugin(it)
			}
			.filter {
				!it.nameWithoutExtension.endsWith(".pu")
			}
		bar.close()
		
		println("\n(worker, info) discovered ${walker.size} plugins for update. updating...")
		bar = ProgressBarBuilder().setStyle(ProgressBarStyle.ASCII).setTaskName("updating").setInitialMax(walker.size.toLong()).build()
		
		var updated = 0
		var failed = 0
		walker.forEach { plugin ->
			try {
				val pluginDirectory = plugin.parentFile
				if (backupOld) {
					val path = Paths.get(pluginDirectory.absolutePath, targetPlugin, "old-versions").toFile()
					path.mkdirs()
					Files.copy(plugin.toPath(), File(path, "$time.pu.jar").toPath())
				}
				Files.delete(plugin.toPath())
				Files.copy(jar.toPath(), File(pluginDirectory, "$targetPlugin-auto-updated.jar").toPath())
				updated += 1
			} catch (e: Exception) {
				failed += 1
				println("(worker, error) failed to update: ${e.message}")
			}
			bar.step()
		}
		
		bar.close()
		println("\n(worker, info) succesfully updated $updated plugins ($failed failed), ${updated + failed}/${walker.size} processed")
	}
	
}