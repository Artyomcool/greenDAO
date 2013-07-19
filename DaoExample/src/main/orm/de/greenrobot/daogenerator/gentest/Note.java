package de.greemrobot.daogenerator.gentest;

import de.greenrobot.daogenerator.annotation.Entity;
import de.greenrobot.daogenerator.annotation.NotNull;
import java.util.Date;

@Entity(table = "Note")
public class Note {
  
  @NotNull
  String text;
  
  String comment;
  
  Date date;
  
}
