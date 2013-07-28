package de.greenrobot.greendao.task;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;
import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import de.greenrobot.daogenerator.SchemaGenerator;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;

public class GenerateDaoSources extends DefaultTask {
  
  @InputDirectory
  def File ormSrcDir;
  
  @Input
  def int schemaVersion;
  
  @Input
  def String genSrcPackage;
  
  @OutputDirectory
  def File genSrcDir;
  
  @TaskAction
  void generateSources() throws Exception {
    SchemaGenerator schemaGenerator = new SchemaGenerator(ormSrcDir, getLogger());
    Schema schema = schemaGenerator.createSchema(schemaVersion, genSrcPackage);
    new DaoGenerator(getLogger()).generateAll(schema, genSrcDir.getAbsolutePath());
  }
  
}
