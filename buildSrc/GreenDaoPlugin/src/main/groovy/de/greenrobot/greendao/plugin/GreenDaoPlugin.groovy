package de.greenrobot.greendao.plugin;

import de.greenrobot.greendao.task.GenerateDaoSources;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

public class GreenDaoPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.task('compileGenerator', type: JavaCompile) << {
      //source = project.files("src-in")
      source.each({file -> print file.name})
    }
    
    project.task('generateDaoSources', type: GenerateDaoSources) {
      inputDir = project.file('input')
      outputDir = project.file('output')
      
    }
    
    project.task('build').dependsOn('compileGenerator')
  }
  
}
