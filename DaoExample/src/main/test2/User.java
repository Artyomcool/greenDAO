package de.greenrobot.daogenerator.gentest.test2;

import de.greenrobot.daogenerator.annotation.Entity;
import de.greenrobot.daogenerator.annotation.NotNull;
import de.greenrobot.daogenerator.annotation.Serialized;
import de.greenrobot.daogenerator.annotation.ToMany;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

@Entity(table = "User")
public class User {
  
  @NotNull
  String name;
  
  @NotNull
  int age;
  
  @Serialized
  PublicKey publicKey;
  
  @Serialized
  PrivateKey privateKey;
  
  @ToMany(relation = "user", orderedBy = "name")
  List<Contact> contactsOrderedByName;
  
}
