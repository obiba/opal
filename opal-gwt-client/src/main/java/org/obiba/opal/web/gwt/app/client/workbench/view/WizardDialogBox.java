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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class WizardDialogBox extends DialogBox {

  private static Translations translations = GWT.create(Translations.class);

  private DockLayoutPanel contentLayout;

  private SimplePanel step;

  private Button finish;

  private Button next;

  private Button previous;

  private Button cancel;

  private Button help;

  private Button close;

  private ResizeHandle resizeHandle;

  private Tooltip helpTooltip;

  private String helpTooltipWidth;

  private String helpTooltipHeight;

  /**
   * 
   */
  public WizardDialogBox() {
    super();
    initWidget();
  }

  /**
   * @param autoHide
   * @param modal
   */
  public WizardDialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    initWidget();
  }

  public void setAutoHide(boolean autoHide) {
    super.setAutoHideEnabled(autoHide);
  }

  /**
   * @param autoHide
   */
  public WizardDialogBox(boolean autoHide) {
    super(autoHide);
    initWidget();
  }

  private void initWidget() {
    addStyleName("wizard");

    super.setWidget(contentLayout = new DockLayoutPanel(Unit.EM));
    setSize("45em", "22em");

    // controls
    initControls();

    // main content
    ScrollPanel scroll = new ScrollPanel();
    contentLayout.add(scroll);
    scroll.setWidget(step = new SimplePanel());
  }

  private void initControls() {
    FlowPanel south;
    contentLayout.addSouth(south = new FlowPanel(), 4);
    south.addStyleName("footer");

    south.add(resizeHandle = new ResizeHandle());
    resizeHandle.makeResizable(contentLayout);

    initNavigationControls(south);
    initHelpControl(south);
  }

  private void initNavigationControls(FlowPanel south) {
    south.add(cancel = new Button(translations.cancelLabel()));
    initControlStyle(cancel, "cancel");
    south.add(close = new Button(translations.closeLabel()));
    initControlStyle(close, "btn-primary cloze");
    close.setVisible(false);
    south.add(finish = new Button(translations.finishLabel()));
    initControlStyle(finish, "btn-primary finish");
    south.add(next = new Button(translations.nextLabel()));
    initControlStyle(next, "btn-info next");
    south.add(previous = new Button(translations.previousLabel()));
    initControlStyle(previous, "btn-info previous");
    setPreviousEnabled(false);
  }

  private void initHelpControl(FlowPanel south) {
    south.add(help = new Button(translations.helpLabel()));
    help.addStyleName("help");
    help.addStyleName("btn");
    help.removeStyleName("gwt-Button");
    help.addStyleName("left-aligned");
    help.addStyleName("top-margin");
    help.setEnabled(false);
  }

  private void initControlStyle(Button button, String style) {
    button.addStyleName(style);
    button.addStyleName("btn");
    button.removeStyleName("gwt-Button");
    button.addStyleName("right-aligned");
    button.addStyleName("top-margin");
  }

  public void setHelpEnabled(boolean enabled) {
    help.setEnabled(enabled);
  }

  public void setFinishEnabled(boolean enabled) {
    finish.setEnabled(enabled);
  }

  public void setNextEnabled(boolean enabled) {
    next.setEnabled(enabled);
  }

  public void setPreviousEnabled(boolean enabled) {
    previous.setEnabled(enabled);
  }

  public void setCancelEnabled(boolean enabled) {
    cancel.setEnabled(enabled);
  }

  public void setCloseEnabled(boolean enabled) {
    close.setEnabled(enabled);
  }

  public void setCloseVisible(boolean visible) {
    close.setVisible(visible);
    finish.setVisible(!visible);
    next.setVisible(!visible);
    previous.setVisible(!visible);
  }

  @Override
  public void setSize(String width, String height) {
    contentLayout.setSize(width, height);
  }

  @Override
  public void setWidth(String width) {
    contentLayout.setWidth(width);
  }

  @Override
  public void setHeight(String height) {
    contentLayout.setHeight(height);
  }

  public void setStep(Widget w) {
    this.setWidget(w);
  }

  @Override
  public void setWidget(Widget w) {
    w.addStyleName("main");
    step.setWidget(w);
  }

  @Override
  public void add(Widget w) {
    this.setWidget(w);
  }

  public HandlerRegistration addPreviousClickHandler(ClickHandler handler) {
    return previous.addClickHandler(handler);
  }

  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return finish.addClickHandler(handler);
  }

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancel.addClickHandler(handler);
  }

  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return next.addClickHandler(handler);
  }

  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return close.addClickHandler(handler);
  }

  public void setHelpTooltip(Widget w) {
    setHelpTooltip(w, "400px", "400px");
  }

  public void setHelpTooltip(Widget w, String width, String height) {
    if(helpTooltip == null) {
      helpTooltip = new Tooltip();
      help.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent evt) {
          helpTooltip.setPopupPosition(evt.getNativeEvent().getClientX() + 20, evt.getNativeEvent().getClientY() - 400);
          helpTooltip.setSize(helpTooltipWidth, helpTooltipHeight);
          helpTooltip.show();
        }
      });
    }
    help.setEnabled(w != null);
    helpTooltip.clear();
    if(w != null) {
      helpTooltip.add(w);
      this.helpTooltipWidth = width;
      this.helpTooltipHeight = height;
    }
  }

  public void setProgress(boolean progress) {
    if(progress) addStyleName("progress");
    else
      removeStyleName("progress");
  }
}
