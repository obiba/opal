package org.obiba.opal.spi.analysis;

public enum AnalysisStatus {

  IN_PROGRESS, // test is in progress
  PASSED,      // test was successful
  FAILED,     // test has failed
  ERROR,       // test could not be run due to an execution error
  IGNORED      // test execution did not return a result

}
