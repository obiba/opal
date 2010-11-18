/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.TextBox;

public class NumericTextBox extends TextBox {

  private int min = 0;

  private int max = 100;

  private boolean minConstrained = true;

  private boolean maxConstrained = true;

  private int step = 1;

  private KeyUpHandler keyUpHandler = new KeyUpHandler() {

    @Override
    public void onKeyUp(KeyUpEvent event) {
      if(isReadOnly() || !isEnabled()) {
        return;
      }

      if(processKeyCode(event.getNativeEvent().getKeyCode())) {
        cancelKey();
      }
    }

    @SuppressWarnings( { "unchecked", "PMD.NcssMethodCount" })
    private boolean processKeyCode(int keyCode) {
      switch(keyCode) {
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_BACKSPACE:
      case KeyCodes.KEY_DELETE:
      case KeyCodes.KEY_UP:
        increaseValue(step);
        break;
      case KeyCodes.KEY_PAGEUP:
        increaseValue(step * 10);
        break;
      case KeyCodes.KEY_DOWN:
        decreaseValue(step);
        break;
      case KeyCodes.KEY_PAGEDOWN:
        decreaseValue(step * 10);
        break;
      default:
        return false;
      }
      return true;
    }

  };

  private KeyPressHandler keyPressHandler = new KeyPressHandler() {
    @Override
    public void onKeyPress(KeyPressEvent event) {
      if(isReadOnly() || !isEnabled()) {
        return;
      }

      switch(event.getNativeEvent().getKeyCode()) {
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_RIGHT:
      case KeyCodes.KEY_BACKSPACE:
      case KeyCodes.KEY_DELETE:
      case KeyCodes.KEY_UP:
      case KeyCodes.KEY_PAGEUP:
      case KeyCodes.KEY_DOWN:
      case KeyCodes.KEY_PAGEDOWN:
        return;
      }

      cancelKey();
      setValue(getNewText(event.getCharCode()), true);
    }

    private String getNewText(char code) {
      int index = getCursorPos();
      String previousText = getText();
      String newText;
      if(getSelectionLength() > 0) {
        newText = previousText.substring(0, getCursorPos()) + code + previousText.substring(getCursorPos() + getSelectionLength(), previousText.length());
      } else {
        newText = previousText.substring(0, index) + code + previousText.substring(index, previousText.length());
      }
      return newText;
    }
  };

  public NumericTextBox() {
    this(0, 0, 100);
  }

  public NumericTextBox(int value) {
    this(value, 0, 100);
  }

  public NumericTextBox(int value, int min, int max) {
    this(value, min, max, true);
  }

  public NumericTextBox(int value, int min, int max, boolean constrained) {
    this(value, min, max, constrained, constrained);
  }

  public NumericTextBox(int value, int min, int max, boolean minConstrained, boolean maxConstrained) {
    super();

    addKeyPressHandler(keyPressHandler);
    addKeyUpHandler(keyUpHandler);

    this.min = min;
    this.max = max;
    this.minConstrained = minConstrained;
    this.maxConstrained = maxConstrained;

    setValue(formatValue(value), false);
    // setTextAlignment(TextBoxBase.ALIGN_CENTER);
    // setStyleName(Resources.INSTANCE.css().fwFormEntry());
  }

  public void setSteps(int step) {
    this.step = step;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public void setMaxConstrained(boolean maxConstrained) {
    this.maxConstrained = maxConstrained;
  }

  public void setMinConstrained(boolean minConstrained) {
    this.minConstrained = minConstrained;
  }

  protected void increaseValue(int step) {
    if(step == 0) return;
    String value = getText();
    long newValue = parseValue(value);
    newValue += step;
    if(maxConstrained && (newValue > max)) {
      return;
    }
    setValue(formatValue(newValue));
  }

  protected void decreaseValue(int step) {
    if(step == 0) return;

    String value = getText();
    long newValue = parseValue(value);
    newValue -= step;
    if(minConstrained && (newValue < min)) {
      return;
    }
    setValue(formatValue(newValue));
  }

  /**
   * @param value the value to format
   * @return the formatted value
   */
  protected String formatValue(long value) {
    String newValue = String.valueOf(value);
    return newValue;
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    try {
      long newValue = parseValue(value);
      if((maxConstrained && (newValue > max)) || (minConstrained && (newValue < min))) {
        return;
      }
      String prevText = getValue();
      super.setText(formatValue(newValue));
      if(fireEvents) {
        ValueChangeEvent.fireIfNotEqual(this, getValue(), prevText);
      }
    } catch(Exception ex) {
      // Do Nothing
    }
  }

  /**
   * @param value the value to parse
   * @return the parsed value
   */
  protected long parseValue(String value) {
    return Long.valueOf(value);
  }
}
