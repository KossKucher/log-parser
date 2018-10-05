package com.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class Utils {
  
  private static final String REPORT_PATTERN = "Start time: %s, End time: %s, ID: %s," +
          " Start full line: %s, End full line: %s.";
  
  private Utils() {
  
  }
  
  static List<File> processArgs(String[] args) {
    List<File> files = new ArrayList<>();
    Arrays.stream(args).forEach(arg -> {
      List<File> pulledFiles = pullFilesFromArg(arg);
      if (pulledFiles == null) {
        System.out.println(String.format("File '%s' is not found!", arg));
      } else {
        files.addAll(pulledFiles);
      }
    });
    return files;
  }
  
  private static List<File> pullFilesFromArg(String arg) {
    File[] userDirFiles = new File(System.getProperty("user.dir")).listFiles();
    File start = null;
    if (userDirFiles != null) {
      start = Arrays.stream(userDirFiles)
                    .filter(file -> file.getName().equals(arg))
                    .findAny().orElse(null);
    }
    if (start != null && start.isFile()) {
      return Collections.singletonList(start);
    } else if (start != null && start.isDirectory()) {
      return pullFilesFromDirTree(start);
    }
    start = new File(arg);
    if (start.isFile()) {
      return Collections.singletonList(start);
    } else if (start.isDirectory()) {
      return pullFilesFromDirTree(start);
    }
    return null;
  }
  
  private static List<File> pullFilesFromDirTree(File rootDir)
          throws IllegalArgumentException {
    if (rootDir == null || !rootDir.isDirectory()) {
      throw new IllegalArgumentException();
    }
    List<File> result = new ArrayList<>();
    try {
      result.addAll(Files.walk(Paths.get(rootDir.getAbsolutePath()))
                         .map(Path::toFile)
                         .filter(file -> (file.isFile() && file.getName().endsWith(".txt")))
                         .collect(Collectors.toCollection(ArrayList::new)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
  
  static List<LogEntry> readLogFile(File file, LogType[] typesToCatch) {
    List<LogEntry> entries = new ArrayList<>();
    try {
      Files.lines(file.toPath(), StandardCharsets.UTF_8)
           .forEach(line -> Arrays.stream(typesToCatch).forEach(type -> {
             if (line.contains(type.toString())) {
               entries.add(new LogEntry(line, type));
             }
           }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return entries;
  }
  
  static List<LogEntry[]> extractPairs(List<LogEntry> entryGroup, LogType primaryType,
                                       LogType secondaryType) {
    List<LogEntry[]> pairs = new ArrayList<>();
    if (entryGroup == null || entryGroup.size() == 0) {
      return pairs;
    }
    LogEntry[] pair = new LogEntry[2];
    pair[1] = entryGroup.stream()
                        .filter(logEntry -> logEntry.getType().equals(primaryType))
                        .findFirst().orElse(null);
    if (pair[1] == null) {
      pair[0] = entryGroup.stream()
                          .filter(logEntry -> logEntry.getType().equals(secondaryType))
                          .findFirst().orElse(null);
      pairs.add(pair);
      return pairs;
    } else {
      List<LogEntry> secondary
              = entryGroup.stream()
                          .filter(logEntry -> logEntry.getType().equals(secondaryType)
                                  && logEntry.getTimestamp().isBefore(pair[1].getTimestamp()))
                          .collect(Collectors.toList());
      if (secondary.size() > 0) {
        pair[0] = secondary.get(0);
        entryGroup.removeAll(secondary);
      } else {
        pair[0] = null;
      }
      pairs.add(pair);
      entryGroup.remove(pair[1]);
      pairs.addAll(extractPairs(entryGroup, primaryType, secondaryType));
    }
    return pairs;
  }
  
  static String makeReport(List<LogEntry[]> pairs) {
    String startTimeString;
    String endTimeString;
    String startFull;
    String endFull;
    String id;
    StringBuilder sb = new StringBuilder();
    for (LogEntry[] pair : pairs) {
      if (pair[0] == null) {
        startTimeString = " --- ";
        startFull = (" --- ");
      } else {
        startTimeString = pair[0].getTimestampString();
        startFull = pair[0].getFullLine();
      }
      if (pair[1] == null) {
        endTimeString = " --- ";
        endFull = (" --- ");
      } else {
        endTimeString = pair[1].getTimestampString();
        endFull = pair[1].getFullLine();
      }
      id = (pair[0] != null) ? pair[0].getId() : pair[1].getId();
      sb.append(String.format(REPORT_PATTERN, startTimeString, endTimeString, id, startFull, endFull))
        .append(System.lineSeparator());
    }
    return sb.toString();
  }
  
  static boolean writeReport(String report, String fileName) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss",
                                                              Locale.ENGLISH);
    String timestamp = ZonedDateTime.now(ZoneId.systemDefault()).format(formatter);
    String fullName = String.format("%s-[%s].csv", fileName, timestamp);
    Path targetFile = Paths.get(System.getProperty("user.dir"), fullName);
    boolean result;
    try (BufferedWriter bw = Files.newBufferedWriter(targetFile)) {
      bw.write(report);
      result = true;
    } catch (IOException e) {
      e.printStackTrace();
      result = false;
    }
    return result;
  }
  
}
