package com.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Runner {
  
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("No target file/dir provided. Exiting...");
      return;
    }
    List<File> files = Utils.processArgs(args);
    List<LogEntry> entries = new ArrayList<>();
    files.forEach(file -> entries.addAll(Utils.readLogFile(file, LogType.values())));
    entries.sort(Comparator.comparing(LogEntry::getTimestamp));
    List<LogEntry[]> pairs = new ArrayList<>();
    entries.stream().collect(Collectors.groupingBy(LogEntry::getId))
           .forEach((k, v) -> pairs.addAll(Utils.extractPairs(v, LogType.PATIENT_DISCHARGED,
                                                              LogType.NEW_PATIENT)));
    pairs.sort((a, b) -> {
      LogEntry first = (a[0] != null) ? a[0] : a[1];
      LogEntry second = (b[0] != null) ? b[0] : b[1];
      return first.getTimestamp().compareTo(second.getTimestamp());
    });
    
    //print out
    String report = Utils.makeReport(pairs);
    Utils.writeReport(report, "template1");
    System.out.println(report);
    System.out.println("Total lines: " + pairs.size());
  }
  
}
