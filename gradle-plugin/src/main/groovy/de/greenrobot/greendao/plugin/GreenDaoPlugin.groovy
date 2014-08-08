package de.greenrobot.greendao.plugin

import de.greenrobot.greendao.task.GenerateDaoSources
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

public class GreenDaoPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    Task generateDaoSources = project.tasks.create(name: 'generateDaoSources', type: GenerateDaoSources) {
      description = 'Generates the source files for the object-relation mapping.'
      ormSrcDir = project.file('src/main/orm');
      genSrcPackage = "orm.gensrc";
      genSrcDir = new File(project.buildDir, 'generated/source/gen-src');
    }

    project.afterEvaluate {
      project.android.applicationVariants.all { variant ->
          variant.javaCompile.dependsOn(generateDaoSources);
      }
      project.android.sourceSets.main.java.srcDirs += generateDaoSources.genSrcDir;
    }
  }
  
}
