package org.obiba.opal.web.gwt.app.client.permissions.support.events;

import java.util.List;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Will generate {@link UpdateResourcePermission} and {@link UpdateResourcePermission.PermissionUpdatedHandler}
 */
@GenEvent
public class UpdateResourcePermission {
  List<String> subjectPrincipals;
  String subjectType;
  String permission;
}
