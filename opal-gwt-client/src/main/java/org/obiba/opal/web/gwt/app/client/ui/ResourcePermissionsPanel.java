package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Iterator;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.Well;
import com.github.gwtbootstrap.client.ui.constants.WellSize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

import edu.umd.cs.findbugs.annotations.Nullable;

public class ResourcePermissionsPanel extends Composite {

  static final Translations translations = GWT.create(Translations.class);

  private Panel permissions;

  private ResourcePermissionType type;

  private String selectedPermission;

  private Handler handler;

  public ResourcePermissionsPanel() {
    permissions = new FlowPanel();
    initWidget(permissions);
  }

  public void addHandler(Handler handler) {
    this.handler = handler;
  }

  public void initialize(@Nonnull ResourcePermissionType type, @Nullable String currentPermission) {
    this.type = type;
    selectedPermission = currentPermission;
    createPermissionRadios();
  }

  private void createPermissionRadios() {
    permissions.clear();
    Well well = new Well();
    well.setSize(WellSize.SMALL);
    well.addStyleName("no-bottom-margin");
    permissions.add(well);
    boolean addTopMargin = false;

    for (Iterator<AclAction> iterator = type.getPermissions().iterator(); iterator.hasNext();) {
      String permission = iterator.next().getName();
      boolean select = selectedPermission != null && permission.equals(selectedPermission);
      well.add(createPermissionPanel(permission, select, addTopMargin));
      addTopMargin = true;
    }
  }

  private Panel createPermissionPanel(final String permissionKey, boolean select, boolean addTopMargin) {
    Panel panel = new FlowPanel();
    if (addTopMargin) panel.setStyleName("large-top-margin");
    final RadioButton radio = new RadioButton("permission", translations.permissionMap().get(permissionKey));
    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (handler != null) handler.onSelected(permissionKey);
      }
    });

    radio.addStyleName("no-bottom-margin");
    radio.setValue(select);
    HelpBlock help = new HelpBlock(translations.permissionMap().get(permissionKey));

    panel.add(radio);
    panel.add(help);

    return panel;
  }

  public interface Handler {
    void onSelected(String permission);
  }

}
