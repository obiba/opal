package org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.TextArea;

public class InnerAutoCompleteTextArea extends TextArea {

  private List<String> suggestions = new ArrayList<String>();

  private String previousWholeText = "";

  private int currentSuggestionPosition = 0;

  private String current = "";

  private boolean backspacePressed = false;

  public InnerAutoCompleteTextArea() {
    addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(!Strings.isNullOrEmpty(current)) {
          switch(event.getNativeKeyCode()) {
          case KeyCodes.KEY_ENTER:
          case KeyCodes.KEY_UP:
          case KeyCodes.KEY_DOWN:
            InnerAutoCompleteTextArea.this.cancelKey();
            break;

          case KeyCodes.KEY_ESCAPE:
          case KeyCodes.KEY_LEFT:
          case KeyCodes.KEY_RIGHT:
            current = "";
            break;
          }
        }
        backspacePressed = (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE);
      }
    });
  }

  @Override
  public String getText() {
    int cursorPosition = getCursorPos();
    String text = super.getText();
    if(cursorPosition == 0) {
      previousWholeText = text;
      return text;
    }
    if(text.equals(previousWholeText)) {
      return current;
    }
    if(Strings.isNullOrEmpty(text)) {
      current = "";
      return text;
    }
    previousWholeText = text;
    if(backspacePressed) {
      if(!Strings.isNullOrEmpty(current)) current = current.substring(0, current.length() - 1);
    } else {
      char charAt = text.charAt(cursorPosition - 1);
      current += Character.toString(charAt);
    }
    currentSuggestionPosition = cursorPosition;
    if(!isSuggestionMatch()) current = "";
    return current;
  }

  @Override
  public void setText(String textArg) {
    String superText = super.getText();
    String start = superText.substring(0, currentSuggestionPosition);
    String end = superText.substring(currentSuggestionPosition, superText.length());
    String text = textArg.substring(current.length(), textArg.length());
    super.setText(start + text + end);
    current = "";
    setCursorPos(currentSuggestionPosition + text.length());
  }

  private boolean isSuggestionMatch() {
    for(String suggestion : suggestions) {
      if(suggestion.startsWith(current)) return true;
    }
    return false;
  }

  public void addSuggestion(String suggestion) {
    suggestions.add(suggestion);
  }

  @Override
  /**
   * Intern for InnerAutoCompleteTextArea. Not part of API...
   */
  public void setSelectionRange(int pos, int length) {
    if(!isAttached()) {
      return;
    }
    getImpl().setSelectionRange(getElement(), pos, length);
  }
}