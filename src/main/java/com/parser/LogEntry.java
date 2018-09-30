package com.parser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {
  
  private static final String TIMESTAMP_PATTERN = "^(.+[\\d:]{8} \\d{4})";
  private static final String TIME_PARSE_PATTERN = "MMM d HH:mm:ss yyyy";
  private static final String ID_PATTERN = "(\\b\\d+-\\w{1,3}-\\w+\\b)";
  
  private LogType type;
  private String fullLine;
  private String timeStamp;
  private String id;
  private Instant instant;
  
  public LogEntry(String logLine, LogType type) throws RuntimeException {
    fullLine = logLine;
    this.type = type;
    Matcher matcher = Pattern.compile(TIMESTAMP_PATTERN).matcher(fullLine);
    if (matcher.find()) {
      timeStamp = matcher.group(1);
    } else {
      throw new RuntimeException("Timestamp pattern matching failed! - Line: " + fullLine);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_PARSE_PATTERN, Locale.ENGLISH);
    instant = LocalDateTime.parse(timeStamp.replaceAll("  ", " "), formatter)
                           .toInstant(ZoneOffset.UTC);
    matcher = Pattern.compile(ID_PATTERN).matcher(fullLine);
    if (matcher.find()) {
      id = matcher.group(1);
    } else {
      throw new RuntimeException("ID pattern matching failed! - Line: " + fullLine);
    }
  }
  
  public String getFullLine() {
    return fullLine;
  }
  
  public Instant getTimestamp() {
    return instant;
  }
  
  public String getTimestampString() {
    return timeStamp;
  }
  
  public LogType getType() {
    return type;
  }
  
  public String getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return fullLine;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LogEntry other = (LogEntry) o;
    return Objects.equals(getFullLine(), other.getFullLine());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(getFullLine());
  }
}
