package de.greenrobot.daogenerator.gentest.test2;

import de.greenrobot.daogenerator.annotation.Entity;
import de.greenrobot.daogenerator.annotation.NotNull;
import de.greenrobot.daogenerator.annotation.Serialized;
import de.greenrobot.daogenerator.annotation.ToOne;
import java.security.PublicKey;
import java.util.Date;

@Entity(table = "Contact")
public class Contact {
  
  @ToOne
  User user;
  
  Date creationDate;
  
  @NotNull
  String name;
  
  @Serialized
  PublicKey publicKey;
  
}
