### Rally plugin for Intellij

Implements Intellij's [task and contexts api](http://www.jetbrains.com/idea/webhelp/managing-tasks-and-context.html)
for Rally ALM.

This software is released under the [MIT License](license.txt).

### Installation and Use
Visit http://rallysoftware.github.io/intellij-plugin for beta builds and instructions.

### Development

Development requires the most recent version of IntelliJ IDEA and a Gradle installation. Gradle manages
third party dependencies by downloading jars into directories already included in the checked-in
IntelliJ project file.

Getting started from a fresh checkout
* Run ```gradle``` This will pull down test and compile dependencies
* Open project from within IntelliJ
* Use the 'Rally ALM Tasks Integration' run configuration to launch
* Use 'Build -> Prepare Plugin...' to generate an installable binary

### Resources
* http://www.jetbrains.org/display/IJOS/Source+Repository+Layout
* http://confluence.jetbrains.net/display/IDEADEV/IntelliJ+IDEA+Plugin+Structure#IntelliJIDEAPluginStructure-PluginExtensions
* http://git.jetbrains.org/?p=idea/community.git;a=blob;f=platform/platform-resources/src/META-INF/LangExtensionPoints.xml;hb=HEAD
* http://git.jetbrains.org/?p=idea/community.git;a=blob;f=platform/platform-resources/src/META-INF/PlatformExtensionPoints.xml;hb=HEAD
