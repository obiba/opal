package org.obiba.opal.core.domain;

import com.google.common.base.Strings;

public enum VCFSampleRole {
  SAMPLE,
  CONTROL;

  public static boolean isSample(String value) {
    return !Strings.isNullOrEmpty(value) && SAMPLE == valueOf(value.toUpperCase());
  }

  public static boolean isControl(String value) {
    return !Strings.isNullOrEmpty(value) && CONTROL == valueOf(value.toUpperCase());
  }
}
