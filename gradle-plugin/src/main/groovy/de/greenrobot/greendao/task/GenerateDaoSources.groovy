package de.greenrobot.greendao.task
import de.greenrobot.daogenerator.DaoGenerator
import de.greenrobot.daogenerator.Schema
import de.greenrobot.daogenerator.SchemaGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public class GenerateDaoSources extends DefaultTask {
  
  @InputDirectory
  def File ormSrcDir;
  
  @Input
  def String genSrcPackage;
  
  @OutputDirectory
  def File genSrcDir;
  
  @TaskAction
  void generateSources() throws Exception {
    SchemaGenerator schemaGenerator = new SchemaGenerator(ormSrcDir) {
        @Override
        protected void debug(String text) {
            getLogger().debug(text);
        }
    };
    Schema schema = schemaGenerator.createSchema(genSrcPackage);
    new DaoGenerator(){
        @Override
        protected void info(String text) {
            getLogger().info(text);
        }

        @Override
        protected void error(String text) {
            getLogger().error(text);
        }
    }.generateAll(schema, genSrcDir.getAbsolutePath());
  }
  
}
