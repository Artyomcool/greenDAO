package de.greenrobot.daogenerator.gentest.test2;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.SchemaGenerator;

public class Main {

  public static void main(String[] args) throws Exception {
    SchemaGenerator schemaGenerator = new SchemaGenerator("src");
    Schema schema = schemaGenerator.createSchema(1, "de.greenrobot.daoexample2");
    new DaoGenerator().generateAll(schema, "src-gen");
  }
  
}
