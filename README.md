# PluginUpdater
Simple Java (Kotlin in fact) script for updating all spigot plugins at once by simply running one command in terminal

## Building
Clone the repo with `git clone` or any other way, build the project with `gradlew build` and get your executable jar in `build/libs`

## Usage
All PluginUpdater's stuff happens via CLI, and to work the program itself requires some arguments. These arguments are:
- the plugin we want to update, e.g. `ViaVersion` or `LuckPerms`
- target directory where we recursively find all .jar files that contains `plugin.yml` with `name: <first argument>` in it. This (second one, to be certain) argument indicates root server directory what means that all suitable jar files in all subdirectories will be included and (as a result) updated.
- determines if we should to backup old version or not. If set to true, original jar file we are updating will be copied to `<plugin folder>/old-versions/<original jar file>` and then replaced with the new one. If false - it will be replaced, but won't be copied to seperate folder

Basically to use the script you need to place the plugin file alongside with updater jar the way they are in one folder and run it from there.

Example command: `java -jar plugin-updater.jar ViaVersion /home/user/servers true`

Tip: You can launch jar file in console (with `java -jar <plugin updater jar>`) with no arguments to see the help page.
