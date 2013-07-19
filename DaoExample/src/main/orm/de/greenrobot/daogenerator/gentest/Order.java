package de.greemrobot.daogenerator.gentest;

import de.greenrobot.daogenerator.annotation.Entity;
import de.greenrobot.daogenerator.annotation.NotNull;
import de.greenrobot.daogenerator.annotation.ToOne;
import java.util.Date;

@Entity(table = "_Order")
public class Order {
  
  Date date;
  
  @NotNull
  @ToOne
  Customer customer;
  
}
