/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 */
public class AlertPanel extends FlowPanel {

  private Anchor close;

  public AlertPanel() {
    addStyleName("alert");
  }

  public void setType(String type) {
    if(type != null && type.isEmpty() == false) {
      addStyleName("alert-" + type.toLowerCase());
    }
  }

  public void add(String message) {
    add(new Label(message));
  }

  public void setCloseable(boolean closeable) {
    if(close != null) {
      remove(close);
      close = null;
    }
    if(closeable) {
      close = new Anchor("x");
      close.setStyleName("close");
      close.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          removeFromParent();
        }
      });
      insert(close, 0);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final AlertPanel alert;

    private final List<String> messageList = new ArrayList<String>();

    Builder() {
      alert = new AlertPanel();
    }

    public Builder title(String title) {
      InlineLabel l = new InlineLabel(title);
      l.addStyleName("bold");
      alert.add(l);
      return this;
    }

    public Builder error(String... messages) {
      alert.setType("error");
      messages(messages);
      return this;
    }

    public Builder warning(String... messages) {
      alert.setType("error");
      messages(messages);
      return this;
    }

    public Builder success(String... messages) {
      alert.setType("success");
      messages(messages);
      return this;
    }

    public Builder info(String... messages) {
      alert.setType("info");
      messages(messages);
      return this;
    }

    public Builder messages(String... messages) {
      if(messages != null) {
        for(String message : messages) {
          messageList.add(message);
        }
      }
      return this;
    }

    public Builder closeable() {
      alert.setCloseable(true);
      return this;
    }

    public AlertPanel build() {
      if(messageList.size() == 1) {
        alert.add(messageList.get(0));
      } else {
        UList ul = new UList();
        for(String msg : messageList) {
          ListItem li = new ListItem();
          li.setText(msg);
          ul.add(li);
        }
        alert.add(ul);
      }
      return alert;
    }

  }

}
