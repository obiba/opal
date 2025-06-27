package org.obiba.opal.core.domain.kubernetes;

import com.google.common.base.Strings;

public class Toleration {

  public enum TaintEffect {
    NoSchedule,
    PreferNoSchedule,
    NoExecute
  }

  public enum TolerationOperator {
    Equal,
    Exists
  }

  private String key;
  private TolerationOperator operator = TolerationOperator.Equal;
  private String value;
  private TaintEffect effect =  TaintEffect.NoSchedule;
  private Long tolerationSeconds; // Nullable if not set

  // Constructors
  public Toleration() {}

  public Toleration(String key, TolerationOperator operator, String value,
                    TaintEffect effect, Long tolerationSeconds) {
    this.key = key;
    this.operator = operator;
    this.value = value;
    this.effect = effect;
    this.tolerationSeconds = tolerationSeconds;
  }

  // Getters and setters
  public boolean hasKey() {
    return !Strings.isNullOrEmpty(key);
  }

  public String getKey() {
    return key;
  }

  public Toleration setKey(String key) {
    this.key = key;
    return this;
  }

  public TolerationOperator getOperator() {
    return operator;
  }

  public Toleration setOperator(String operator) {
    if (operator != null) {
      try {
        this.operator = TolerationOperator.valueOf(operator);
      }  catch (IllegalArgumentException e) {
        this.operator = TolerationOperator.Equal;
      }
    }  else {
      this.operator = TolerationOperator.Equal;
    }
    return this;
  }

  public Toleration setOperator(TolerationOperator operator) {
    this.operator = operator;
    return this;
  }

  public boolean hasValue() {
    return !Strings.isNullOrEmpty(value);
  }

  public String getValue() {
    return value;
  }

  public Toleration setValue(String value) {
    this.value = value;
    return this;
  }

  public boolean hasEffect() {
    return !Strings.isNullOrEmpty(effect.toString());
  }

  public TaintEffect getEffect() {
    return effect;
  }

  public Toleration setEffect(String effect) {
    if (effect != null) {
      try {
        this.effect = TaintEffect.valueOf(effect);
      }   catch (IllegalArgumentException e) {
        this.effect = TaintEffect.NoSchedule;
      }
    } else {
      this.effect = TaintEffect.NoSchedule;
    }
    return this;
  }

  public Toleration setEffect(TaintEffect effect) {
    this.effect = effect;
    return this;
  }

  public boolean hasTolerationSeconds() {
    return tolerationSeconds != null;
  }

  public Long getTolerationSeconds() {
    return tolerationSeconds;
  }

  public Toleration setTolerationSeconds(Long tolerationSeconds) {
    this.tolerationSeconds = tolerationSeconds;
    return this;
  }

  @Override
  public String toString() {
    return "Toleration{" +
        "key='" + key + '\'' +
        ", operator=" + operator +
        ", value='" + value + '\'' +
        ", effect=" + effect +
        ", tolerationSeconds=" + tolerationSeconds +
        '}';
  }
}
