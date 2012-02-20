package org.obiba.opal.web.gwt.app.client.wizard.derive.view.widget;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.text.shared.testing.PassthroughParser;
import com.google.gwt.user.client.ui.TextArea;

public class InnerAutoCompleteTextArea extends TextArea {

  private List<String> suggestions;

  private String previousText;

  private int currentSuggestionPosition;

  private String currentSuggestion;

  private boolean backspacePressed;

  private String text;

  public InnerAutoCompleteTextArea() {
    initializeText("");
    addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(Strings.isNullOrEmpty(currentSuggestion) == false) {
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

  @SuppressWarnings("PMD.NcssMethodCount")
  @Override
  public String getText() {
    int cursorPosition = getCursorPos();
    text = super.getText();
    if(text.equals(previousText)) {
      return currentSuggestion;
    }
    if(Strings.isNullOrEmpty(text)) {
      currentSuggestion = "";
      previousText = text;
      return text;
    }
    previousText = text;
    if(backspacePressed) {
      if(Strings.isNullOrEmpty(currentSuggestion) == false) {
        currentSuggestion = currentSuggestion.substring(0, currentSuggestion.length() - 1);
      }
    } else {
      if(cursorPosition == 0) return "";
      char charAt = text.charAt(cursorPosition - 1);
      currentSuggestion += Character.toString(charAt);
    }
    currentSuggestionPosition = cursorPosition;
    if(isSuggestionMatch() == false) currentSuggestion = "";
    return currentSuggestion;
  }

  @Override
  public String getValueOrThrow() throws ParseException {
    String text = getRealText();
    String parseResult = PassthroughParser.instance().parse(text);
    if("".equals(text)) {
      return null;
    }
    return parseResult;
  }

  public boolean isTextSelected() {
    return getSelectionLength() > 0;
  }

  @Override
  public String getSelectedText() {
    int start = getCursorPos();
    if(start < 0) {
      return "";
    }
    int length = getSelectionLength();
    return getRealText().substring(start, start + length);
  }

  public String getRealText() {
    return text == null ? "" : text;
  }

  public void initializeText(String value) {
    text = value;
    super.setText(value);
    suggestions = new ArrayList<String>();
    previousText = "";
    currentSuggestionPosition = 0;
    currentSuggestion = "";
    backspacePressed = false;
    setCursorPos(text.length());
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
    if(isAttached() == false) {
      return;
    }
    getImpl().setSelectionRange(getElement(), pos, length);
  }

}