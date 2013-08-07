package org.obiba.opal.core.domain.taxonomy;

public class Text {

  private String locale;

  private String text;

  public Text(String text, String locale) {
    this.text = text;
    this.locale = locale;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}