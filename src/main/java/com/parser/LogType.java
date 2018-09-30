package com.parser;

public enum LogType {
  NEW_PATIENT("new patient"),
  PATIENT_DISCHARGED("patient discharged");
  
  private String type;
  
  LogType(String type) {
    this.type = type;
  }
  
  @Override
  public String toString() {
    return type;
  }
}
