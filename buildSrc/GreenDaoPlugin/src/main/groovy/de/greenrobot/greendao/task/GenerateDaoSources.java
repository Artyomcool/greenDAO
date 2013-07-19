package de.greenrobot.greendao.task;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;
import java.io.File;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import de.greenrobot.daogenerator.SchemaGenerator;

public class GenerateDaoSources extends DefaultTask {
  
  public File ormSrcDir;
  public int schemaVersion;
  public String genSrcPackage;
  
  public File genSrcDir;
  
  @TaskAction
  public void generateSources() throws Exception {
    genSrcDir.mkdirs();
    
    SchemaGenerator schemaGenerator = new SchemaGenerator(ormSrcDir);
    Schema schema = schemaGenerator.createSchema(schemaVersion, genSrcPackage);
    new DaoGenerator().generateAll(schema, genSrcDir.getAbsolutePath());
  }
  
}
