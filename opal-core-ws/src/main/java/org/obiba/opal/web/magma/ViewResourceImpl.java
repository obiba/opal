/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import org.obiba.opal.web.ws.util.ContentDisposition;
import org.obiba.magma.*;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.magma.js.validation.ScriptReferences;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.views.View;
import org.obiba.opal.core.cfg.OpalViewManager.DependentView;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableRenamedEvent;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.spi.r.datasource.magma.ResourceView;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ViewResourceImpl extends TableResourceImpl implements ViewResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  // same test as the web shell
  private static final String ADMINISTRATOR = "rest:/:POST";

  private ViewDtos viewDtos;

  private SubjectProfileService subjectProfileService;

  @Autowired
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Autowired
  public void setSubjectProfileService(SubjectProfileService subjectProfileService) {
    this.subjectProfileService = subjectProfileService;
  }

  @Override
  public ViewDto getView() {
    return viewDtos.asDto(asValueView());
  }

  @Override
  public Response updateView(ViewDto viewDto, @Nullable String comment) {
    if (!viewDto.hasName())
      return Response.status(Status.BAD_REQUEST).build();
    if (isResourceView() && !viewDto.hasExtension(Magma.ResourceViewDto.view))
      return Response.status(Status.BAD_REQUEST).build();

    if (ReservedNames.isReserved(viewDto.getName()))
      return Response.status(Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "ReservedTableName").build()).build();

    if (!checkValuesPermissions(getValueTable(), viewDto))
      return Response.status(Status.FORBIDDEN).build();
    ensureDerivationPermitted(viewDto, () -> !sameDerivation(viewDtos.asDto(asValueView()), viewDto),
        () -> referencesOf(viewDto));

    ValueTable table = getValueTable();
    if (!viewDto.getName().equals(table.getName())) {
      List<DependentView> dependents = getDependentViews();
      if (!dependents.isEmpty()) return TableDependents.conflict(dependents);
      getEventBus().post(new ValueTableRenamedEvent(table, viewDto.getName()));
      getViewManager().removeView(getDatasource().getName(), getValueTable().getName());
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());
    }
    getViewManager().addView(getDatasource().getName(), viewDtos.fromDto(viewDto), comment, null);

    return Response.ok().build();
  }

  @Override
  public VariablesViewResource getVariables() {
    VariablesViewResource resource = applicationContext.getBean("variablesViewResource", VariablesViewResource.class);
    resource.setLocales(getLocales());
    resource.setValueTable(getValueTable());
    return resource;
  }

  @Override
  public Response removeView() {
    try {
      List<DependentView> dependents = getDependentViews();
      if (!dependents.isEmpty()) return TableDependents.conflict(dependents);
      getEventBus().post(new ValueTableDeletedEvent(getValueTable()));
      getViewManager().removeView(getDatasource().getName(), getValueTable().getName());
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());
    } catch (NoSuchValueTableException e) {
      // ignore
    }
    return Response.ok().build();
  }

  @Override
  public Response initView() {
    getViewManager().initView(getDatasource().getName(), getValueTable().getName());
    return ValueTableStatus.READY.equals(getValueTable().getStatus()) ? Response.ok().build() : Response.serverError().build();
  }

  @Override
  public Response downloadViewDefinition() {
    return Response.ok(asValueView(), "application/xml")
        .header("Content-Disposition", ContentDisposition.attachment(getValueTable().getName() + ".xml")).build();
  }

  @Override
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    TableResource resource = applicationContext.getBean("tableResource", TableResource.class);
    resource.setValueTable(asTableView().getWrappedValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  @Override
  public VariableViewResource getVariable(String name) {
    VariableViewResource resource = applicationContext.getBean(VariableViewResource.class);
    resource.setName(name);
    resource.setValueTable(getValueTable());
    resource.setLocales(getLocales());
    resource.setVariableValueSource(getValueTable().getVariableValueSource(name));
    return resource;
  }

  /**
   * A user who can see the values of the view must also be permitted to see the values of the view sources.
   */
  public static boolean checkValuesPermissions(ValueTable table, ViewDto viewDto) {
    if (!MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) return true;

    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();

    if (!authorizer.isPermitted(
        "rest:/datasource/" + table.getDatasource().getName() + "/table/" + table.getName() + "/valueSet:GET"))
      return true;

    return canReadSources(viewDto);
  }

  /**
   * Whether the user can see the values of every source of the view, whatever the permissions on the view itself.
   */
  static boolean canReadSources(ViewDto viewDto) {
    if (!MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) return true;
    return viewDto.hasExtension(Magma.ResourceViewDto.view) ? checkResourceValuesPermissions(viewDto) : checkTableValuesPermissions(viewDto);
  }

  /**
   * Changing how a view derives its values (scripts, sources, filters, resource columns) needs the values of every
   * source, and of every table its scripts read: otherwise the summaries of the result can single out the values the
   * user is not permitted to see. All the scripts of the resulting view count, not only the changed ones, as a new
   * script or filter can work on what the others read. Administrators can see all values and are not checked.
   */
  public static void ensureDerivationPermitted(ViewDto viewDto, BooleanSupplier changed,
                                               Supplier<? extends Collection<ScriptReferences>> references) {
    if (!MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) return;
    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();
    if (authorizer.isPermitted(ADMINISTRATOR) || !changed.getAsBoolean()) return;
    if (!canReadSources(viewDto) || !canReadReferences(viewDto, references.get()))
      throw new ForbiddenException(Response.status(Status.FORBIDDEN)
          .entity(ClientErrorDtos.getErrorMessage(Status.FORBIDDEN, "ViewDerivationNotPermitted").build()).build());
  }

  /**
   * What the scripts and sameAs of the view read, entity filter and select script included.
   */
  public static List<ScriptReferences> referencesOf(ViewDto viewDto) {
    return referencesOf(viewDto, List.of(), Set.of());
  }

  /**
   * What the scripts of the view read once the variables are saved in it: they replace the variables of the same name
   * and the removed ones (e.g. renamed).
   */
  public static List<ScriptReferences> referencesOf(ViewDto viewDto, Iterable<Variable> saved, Set<String> removed) {
    // the scripts are in a file, see referencesOf(ValueView)
    if (viewDto.hasExtension(Magma.FileViewDto.view)) return List.of(new ScriptReferences(Set.of(), true));
    Map<String, Variable> variables = new LinkedHashMap<>();
    variablesOf(viewDto).forEach(variable -> variables.put(variable.getName(), variable));
    removed.forEach(variables::remove);
    saved.forEach(variable -> variables.put(variable.getName(), variable));
    List<ScriptReferences> refs = referencesOf(variables.values());
    refs.add(ScriptReferences.of(viewDto.getWhere()));
    refs.add(ScriptReferences.of(viewDto.getExtension(Magma.JavaScriptViewDto.view).getSelect()));
    return refs;
  }

  /**
   * What the scripts of a view built from a file read. The view is initialised to get its variables, so it must not be
   * the one that is saved.
   */
  public static List<ScriptReferences> referencesOf(ValueView view) {
    Initialisables.initialise(view);
    List<ScriptReferences> refs = referencesOf(view.getVariables());
    if (view instanceof View tableView) {
      if (tableView.getWhereClause() instanceof JavascriptClause where) refs.add(ScriptReferences.of(where.getScript()));
      if (tableView.getSelectClause() instanceof JavascriptClause select) refs.add(ScriptReferences.of(select.getScript()));
    }
    return refs;
  }

  private static List<ScriptReferences> referencesOf(Iterable<Variable> variables) {
    List<ScriptReferences> refs = new ArrayList<>();
    for (Variable variable : variables) {
      for (Attribute attr : variable.getAttributes()) {
        String value = attr.getValue().isNull() ? "" : attr.getValue().toString();
        if ("script".equals(attr.getName())) refs.add(ScriptReferences.of(value));
        else if ("sameAs".equals(attr.getName())) refs.add(new ScriptReferences(Set.of(value), false));
      }
    }
    return refs;
  }

  /**
   * Whether the user can see the values of every table named by the scripts. A table without datasource is in the
   * datasource of the table the script runs on, so it is checked in the datasource of each source. Variables without
   * table are in the sources, see {@link #canReadSources(ViewDto)}. Scripts that name tables dynamically are left to
   * administrators.
   */
  private static boolean canReadReferences(ViewDto viewDto, Collection<ScriptReferences> references) {
    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();
    for (ScriptReferences refs : references) {
      if (refs.dynamic()) return false;
      for (String variable : refs.variables()) {
        MagmaEngineVariableResolver ref = MagmaEngineVariableResolver.valueOf(variable);
        if (!ref.hasTableName()) continue;
        Collection<String> datasources = ref.hasDatasourceName() ? List.of(ref.getDatasourceName())
            : viewDto.getFromList().stream().map(f -> MagmaEngineTableResolver.valueOf(f).getDatasourceName()).collect(Collectors.toSet());
        for (String datasource : datasources) {
          if (datasource == null || !authorizer.isPermitted(
              "rest:/datasource/" + datasource + "/table/" + ref.getTableName() + "/valueSet:GET")) return false;
        }
      }
    }
    return true;
  }

  /**
   * Whether the updated view derives its values as the current one does. With no current view, whether the view
   * derives nothing on its own, i.e. it only selects the variables of its sources.
   */
  public static boolean sameDerivation(@Nullable ViewDto current, ViewDto updated) {
    if (updated.hasExtension(Magma.FileViewDto.view)) return false;
    boolean resource = updated.hasExtension(Magma.ResourceViewDto.view);
    if (current == null) current = withoutDerivation(updated);
    if (!Set.copyOf(current.getFromList()).equals(Set.copyOf(updated.getFromList()))
        || !Set.copyOf(current.getInnerFromList()).equals(Set.copyOf(updated.getInnerFromList()))
        || !current.getWhere().equals(updated.getWhere())
        || current.hasExtension(Magma.JavaScriptViewDto.view) != updated.hasExtension(Magma.JavaScriptViewDto.view)
        || current.hasExtension(Magma.VariableListViewDto.view) != updated.hasExtension(Magma.VariableListViewDto.view)
        || current.hasExtension(Magma.ResourceViewDto.view) != resource
        || !current.getExtension(Magma.JavaScriptViewDto.view).getSelect()
        .equals(updated.getExtension(Magma.JavaScriptViewDto.view).getSelect())
        || !sameColumns(current.getExtension(Magma.ResourceViewDto.view), updated.getExtension(Magma.ResourceViewDto.view)))
      return false;

    Map<String, Variable> currentVariables = variablesOf(current).stream()
        .collect(Collectors.toMap(Variable::getName, v -> v, (a, b) -> a));
    return variablesOf(updated).stream()
        .allMatch(v -> sameDerivation(currentVariables.get(v.getName()), v, resource));
  }

  /**
   * Whether the updated variable gets its values as the current one does. A variable without script nor sameAs
   * gets no value in a table view, while in a resource view its name selects a column.
   */
  static boolean sameDerivation(@Nullable Variable current, Variable updated, boolean resource) {
    if (resource && (current == null || !current.getName().equals(updated.getName()))) return false;
    return derivationOf(current).equals(derivationOf(updated));
  }

  private static boolean sameColumns(Magma.ResourceViewDto current, Magma.ResourceViewDto updated) {
    return current.getIdColumn().equals(updated.getIdColumn()) && current.getProfile().equals(updated.getProfile())
        && current.getAllColumns() == updated.getAllColumns();
  }

  private static Set<String> derivationOf(@Nullable Variable variable) {
    if (variable == null || !variable.hasAttributes()) return Set.of();
    return variable.getAttributes().stream()
        .filter(attr -> "script".equals(attr.getName()) || "sameAs".equals(attr.getName())
            || (attr.hasNamespace() && "opal".equals(attr.getNamespace()) && "column".equals(attr.getName())))
        .map(attr -> (attr.hasNamespace() ? attr.getNamespace() : "") + ":" + attr.getName() + ":"
            + (attr.isLocalised() ? attr.getLocale() : "") + ":" + attr.getValue())
        .collect(Collectors.toSet());
  }

  private static List<Variable> variablesOf(ViewDto viewDto) {
    List<Magma.VariableDto> dtos = viewDto.hasExtension(Magma.ResourceViewDto.view)
        ? viewDto.getExtension(Magma.ResourceViewDto.view).getVariablesList()
        : viewDto.getExtension(Magma.VariableListViewDto.view).getVariablesList();
    return dtos.stream().map(Dtos::fromDto).collect(Collectors.toList());
  }

  /**
   * Same sources and type of view, with no variables, filter nor select script.
   */
  private static ViewDto withoutDerivation(ViewDto viewDto) {
    ViewDto.Builder builder = ViewDto.newBuilder()
        .addAllFrom(viewDto.getFromList())
        .addAllInnerFrom(viewDto.getInnerFromList());
    if (viewDto.hasExtension(Magma.JavaScriptViewDto.view))
      builder.setExtension(Magma.JavaScriptViewDto.view, Magma.JavaScriptViewDto.getDefaultInstance());
    if (viewDto.hasExtension(Magma.VariableListViewDto.view))
      builder.setExtension(Magma.VariableListViewDto.view, Magma.VariableListViewDto.getDefaultInstance());
    if (viewDto.hasExtension(Magma.ResourceViewDto.view))
      builder.setExtension(Magma.ResourceViewDto.view,
          viewDto.getExtension(Magma.ResourceViewDto.view).toBuilder().clearVariables().build());
    return builder.build();
  }

  private static boolean checkTableValuesPermissions(ViewDto viewDto) {
    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();
    // user can see the values of the view, so make sure user is also permitted to see the referred tables values
    for (String tableName : viewDto.getFromList()) {
      // from the reference, not the resolved table: a source the user cannot see at all does not resolve
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      if (resolver.getDatasourceName() == null || !authorizer.isPermitted(
          "rest:/datasource/" + resolver.getDatasourceName() + "/table/" + resolver.getTableName() + "/valueSet:GET")) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkResourceValuesPermissions(ViewDto viewDto) {
    // user can see the values of the view, so make sure user is also permitted to edit the resource ref
    for (String tableName : viewDto.getFromList()) {
      // reuse project's table naming for the resource...
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      if (!authorizer.isPermitted(
          "rest:/project/" + resolver.getDatasourceName() + "/resource/" + resolver.getTableName() + ":PUT")) {
        return false;
      }
    }
    return true;
  }

  private ValueView asValueView() {
    ValueTable table = getValueTable();
    if (table.isView()) return (ValueView) table;
    throw new InvalidRequestException("Not a value view");
  }

  private View asTableView() {
    ValueTable table = getValueTable();
    if (isTableView()) return (View) table;
    throw new InvalidRequestException("Not a table view");
  }

  private boolean isTableView() {
    ValueTable table = getValueTable();
    return table.isView() && table instanceof View;
  }

  private ResourceView asResourceView() {
    ValueTable table = getValueTable();
    if (isResourceView()) return (ResourceView) table;
    throw new InvalidRequestException("Not a resource view");
  }

  private boolean isResourceView() {
    ValueTable table = getValueTable();
    return table.isView() && table instanceof ResourceView;
  }

}
