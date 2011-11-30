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

  private String previousText = "";

  private int currentSuggestionPosition = 0;

  private String currentSuggestion = "";

  private boolean backspacePressed = false;

  public InnerAutoCompleteTextArea() {
    addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(!Strings.isNullOrEmpty(currentSuggestion)) {
          switch(event.getNativeKeyCode()) {
          case KeyCodes.KEY_ENTER:
          case KeyCodes.KEY_UP:
          case KeyCodes.KEY_DOWN:
            InnerAutoCompleteTextArea.this.cancelKey();
            break;

          case KeyCodes.KEY_ESCAPE:
          case KeyCodes.KEY_LEFT:
          case KeyCodes.KEY_RIGHT:
            currentSuggestion = "";
            break;
          }
        }
        backspacePressed = (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE);
      }
    });
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public String getText() {
    int cursorPosition = getCursorPos();
    String text = super.getText();
    if(cursorPosition == 0) {
      previousText = text;
      return text;
    }
    if(text.equals(previousText)) {
      return currentSuggestion;
    }
    if(Strings.isNullOrEmpty(text)) {
      currentSuggestion = "";
      return text;
    }
    previousText = text;
    if(backspacePressed) {
      if(!Strings.isNullOrEmpty(currentSuggestion)) {
        currentSuggestion = currentSuggestion.substring(0, currentSuggestion.length() - 1);
      }
    } else {
      char charAt = text.charAt(cursorPosition - 1);
      currentSuggestion += Character.toString(charAt);
    }
    currentSuggestionPosition = cursorPosition;
    if(!isSuggestionMatch()) currentSuggestion = "";
    return currentSuggestion;
  }

  @Override
  public void setText(String textArg) {
    String superText = super.getText();
    String start = superText.substring(0, currentSuggestionPosition);
    String end = superText.substring(currentSuggestionPosition, superText.length());
    String text = textArg.substring(currentSuggestion.length(), textArg.length());
    super.setText(start + text + end);
    currentSuggestion = "";
    setCursorPos(currentSuggestionPosition + text.length());
  }

  private boolean isSuggestionMatch() {
    for(String suggestion : suggestions) {
      if(suggestion.startsWith(currentSuggestion)) return true;
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