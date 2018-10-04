package org.obiba.opal.spi.analysis;

public enum AnalysisStatus {

  IN_PROGRESS, // test is on going
  SUCCESS, // test was successful
  WARNING, // test is positive but there are some notifications
  FAILURE  // test has failed

}
