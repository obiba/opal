/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.support.yaml;

import com.google.common.collect.ImmutableSet;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public abstract class AbstractYaml<T> extends Yaml {

  private final Class<T> type;

  protected AbstractYaml(Class<T> type) {
    super(new Constructor(new TLoaderOptions(tag -> tag.matches(type))), new TRepresenter(), new TDumperOptions());
    this.type = type;
  }

  protected Class<T> getType() {
    return type;
  }

  @Override
  public T load(InputStream io) {
    return loadAs(io, getType());
  }

  @Override
  public T load(Reader io) {
    return loadAs(io, getType());
  }

  @Override
  public T load(String yaml) {
    return loadAs(yaml, getType());
  }

  private static class TPropertyUtils extends PropertyUtils {
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
      Map<String, Property> propertyMap = getPropertiesMap(type, BeanAccess.DEFAULT);

      ImmutableSet.Builder<Property> builder = ImmutableSet.builder();
      addProperty("name", propertyMap, builder);
      addProperty("version", propertyMap, builder);
      addProperty("title", propertyMap, builder);
      addProperty("description", propertyMap, builder);
      builder.addAll(propertyMap.values());
      return builder.build();
    }

    private void addProperty(String key, Map<String, Property> propertyMap, ImmutableSet.Builder<Property> builder) {
      if(propertyMap.containsKey(key)) {
        builder.add(propertyMap.remove(key));
      }
    }
  }

  private static class TRepresenter extends Representer {

    private TRepresenter() {
      super(new TDumperOptions());
      setPropertyUtils(new TPropertyUtils());
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
        Tag customTag) {
      if(propertyValue == null) return null;
      if(propertyValue instanceof Boolean && !(Boolean) propertyValue) return null;
      return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
    }
  }

  private static class TDumperOptions extends DumperOptions {
    private TDumperOptions() {
      setDefaultFlowStyle(FlowStyle.BLOCK);
      setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
    }
  }

  private static class TLoaderOptions extends LoaderOptions {

    private TLoaderOptions(TagInspector inspector) {
      super();
      setTagInspector(inspector);
    }

  }

}
