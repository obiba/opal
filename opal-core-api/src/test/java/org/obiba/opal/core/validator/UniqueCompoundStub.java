/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.validator;

import java.util.List;

import org.obiba.opal.core.domain.HasUniqueProperties;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@Unique(compoundProperties = @Unique.CompoundProperty(name = "unique prop",
    properties = { "sub1.prop1", "sub2.prop2" }))
public class UniqueCompoundStub implements HasUniqueProperties {

  private String name;

  private Sub1 sub1;

  private Sub2 sub2;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Sub1 getSub1() {
    return sub1;
  }

  public void setSub1(Sub1 sub1) {
    this.sub1 = sub1;
  }

  public Sub2 getSub2() {
    return sub2;
  }

  public void setSub2(Sub2 sub2) {
    this.sub2 = sub2;
  }

  @Override
  public int hashCode() {return Objects.hashCode(name);}

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equal(name, ((UniqueCompoundStub) obj).name);
  }

  @Override
  public List<String> getUniqueProperties() {
    return Lists.newArrayList("name");
  }

  @Override
  public List<Object> getUniqueValues() {
    return Lists.<Object>newArrayList(name);
  }

  public static class Sub1 {
    private String prop1;

    public String getProp1() {
      return prop1;
    }

    public void setProp1(String prop1) {
      this.prop1 = prop1;
    }
  }

  public static class Sub2 {
    private String prop2;

    public String getProp2() {
      return prop2;
    }

    public void setProp2(String prop2) {
      this.prop2 = prop2;
    }
  }

}
