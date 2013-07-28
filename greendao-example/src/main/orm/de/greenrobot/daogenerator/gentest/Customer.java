package de.greemrobot.daogenerator.gentest;

import de.greenrobot.daogenerator.annotation.Entity;
import de.greenrobot.daogenerator.annotation.NotNull;
import java.util.List;

@Entity(table = "Customer")
public class Customer {
  
  @NotNull
  String name;
  
  @ToMany(relation = "customer", orderedBy = "date")
  List<Order> ordersSortedByDate;
  
}
