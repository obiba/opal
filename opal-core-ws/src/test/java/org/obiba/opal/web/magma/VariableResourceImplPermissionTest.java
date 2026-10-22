package org.obiba.opal.web.magma;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VariableResourceImplPermissionTest {

  @Before
  public void bindSubject() {
    Subject subject = createMock(Subject.class);
    expect(subject.isPermitted("rest:/datasource/ds/table/t/valueSets:GET")).andReturn(true).anyTimes();
    expect(subject.isPermitted(anyString())).andReturn(false).anyTimes();
    replay(subject);
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void test_values_permission_checked_on_table_value_sets() {
    assertTrue(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/t/variable/v/summary"));
    assertTrue(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/t/variable/v/summary/"));
    // regex characters in the variable name
    assertTrue(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/t/variable/a+b(1)/summary"));
    assertFalse(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/other/variable/v/summary"));
  }

  @Test
  public void test_unexpected_path_denied() {
    assertFalse(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/t/variable/v/summary/x"));
    assertFalse(VariableResourceImpl.isViewValuesPermitted("/datasource/ds/table/t/valueSets"));
  }

}
