package de.greenrobot.greendao.task

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class GenerateDaoSources extends DefaultTask {
  
  def File inputDir
  def File outputDir
  
  @TaskAction
  def void generateSources() {
    println "Generates DAO sources."
  }
  
}
