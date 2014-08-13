/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui;

import javax.annotation.Nullable;

import com.github.gwtbootstrap.client.ui.base.TextBox;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;


public class NumericTextBox extends TextBox {

  private int min = 0;

  private int max = 100;

  private boolean minConstrained = true;

  private boolean maxConstrained = true;

  private int step = 1;

  private NumberType numberType = NumberType.INTEGER;

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

    addKeyPressHandler(new NumericKeyPressHandler());
    addKeyUpHandler(new NumericKeyUpHandler());

    this.min = min;
    this.max = max;
    this.minConstrained = minConstrained;
    this.maxConstrained = maxConstrained;

    setValue(String.valueOf(value), false);
  }

  public void setNumberType(NumberType numberType) {
    this.numberType = numberType;
  }

  public void setNumberType(String type) {
    numberType = NumberType.valueOf(type.toUpperCase());
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
    Number newValue = numberType.increaseValue(value, step);
    if(newValue == null || maxConstrained && newValue.intValue() > max) {
      return;
    }
    setValue(numberType.formatValue(newValue));
  }

  protected void decreaseValue(int step) {
    if(step == 0) return;
    String value = getText();
    Number newValue = numberType.decreaseValue(value, step);
    if(newValue == null || minConstrained && newValue.intValue() < min) {
      return;
    }
    setValue(numberType.formatValue(newValue));
  }

  public void setValue(int value) {
    setValue(String.valueOf(value), false);
  }

  @Override
  public void setValue(String value) {
    setValue(value, false);
  }

  @Override
  public void setValue(String value, boolean fireEvents) {
    try {
      Number newValue = numberType.parseValue(value);
      if(newValue == null || maxConstrained && newValue.intValue() > max ||
          minConstrained && newValue.intValue() < min) {
        return;
      }
      String prevText = getValue();
      setText(numberType.formatValue(newValue));
      if(fireEvents) {
        ValueChangeEvent.fireIfNotEqual(this, getValue(), prevText);
      }
    } catch(Exception ex) {
      // Do Nothing
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T extends Number> T getNumberValue() {
    if(getText().isEmpty()) return null;
    return (T) numberType.parseValue(getValue());
  }

  /**
   *
   */
  private final class NumericKeyPressHandler implements KeyPressHandler {
    @Override
    public void onKeyPress(KeyPressEvent event) {
      if(isReadOnly() || !isEnabled() || event.getNativeEvent().getCtrlKey() || event.getNativeEvent().getAltKey()) {
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
        case KeyCodes.KEY_TAB:
          return;
      }

      String newText = getNewText(event.getCharCode());
      if("-".equals(newText) || numberType == NumberType.DECIMAL && newText.endsWith(".") && newText.length() > 1 &&
          !newText.substring(0, newText.length() - 1).contains(".")) {
        return;
      }

      cancelKey();
      setValue(newText, true);
    }

    private String getNewText(char code) {
      int index = getCursorPos();
      String previousText = getText();
      String newText;
      newText = getSelectionLength() > 0
          ? previousText.substring(0, getCursorPos()) + code +
          previousText.substring(getCursorPos() + getSelectionLength(), previousText.length())
          : previousText.substring(0, index) + code + previousText.substring(index, previousText.length());
      return newText;
    }
  }

  /**
   *
   */
  private final class NumericKeyUpHandler implements KeyUpHandler {
    @Override
    public void onKeyUp(KeyUpEvent event) {
      if(isReadOnly() || !isEnabled()) {
        return;
      }

      if(processKeyCode(event.getNativeEvent().getKeyCode())) {
        cancelKey();
      }
    }

    @SuppressWarnings({ "unchecked", "PMD.NcssMethodCount" })
    private boolean processKeyCode(int keyCode) {
      switch(keyCode) {
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
  }

  public enum NumberType {
    INTEGER {
      @Nullable
      @Override
      public Number parseValue(String value) {
        try {
          return Long.valueOf(value);
        } catch(Exception ex) {
          return null;
        }
      }

      @Nullable
      @Override
      public Number increaseValue(String value, int step) {
        try {
          return Long.valueOf(value) + step;
        } catch(Exception ex) {
          return null;
        }
      }

      @Nullable
      @Override
      public Number decreaseValue(String value, int step) {
        try {
          return Long.valueOf(value) - step;
        } catch(Exception ex) {
          return null;
        }
      }
    },
    DECIMAL {
      @Override
      public Number parseValue(String value) {
        try {
          String str = value;
          if(value.endsWith(".")) {
            str += "0";
          }
          return Double.valueOf(str);
        } catch(Exception ex) {
          return null;
        }
      }

      @Override
      public Number increaseValue(String value, int step) {
        try {
          return Double.valueOf(value) + step;
        } catch(Exception ex) {
          return null;
        }
      }

      @Override
      public Number decreaseValue(String value, int step) {
        try {
          return Double.valueOf(value) - step;
        } catch(Exception ex) {
          return null;
        }
      }

      @Override
      public String formatValue(Number value) {
        String str = super.formatValue(value);
        if(str.endsWith(".0")) {
          return str.substring(0, str.length() - 2);
        }
        return str;
      }
    };

    @Nullable
    public abstract Number parseValue(String value);

    public String formatValue(Number value) {
      return value.toString();
    }

    @Nullable
    public abstract Number increaseValue(String value, int step);

    @Nullable
    public abstract Number decreaseValue(String value, int step);
  }
}
