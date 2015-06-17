/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.rest.client.magma;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Datasource;
import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VectorSource;
import org.obiba.magma.VectorSourceNotSupportedException;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Math;
import org.obiba.opal.web.model.Search;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class RestValueTable extends AbstractValueTable {

  //private static final Logger log = LoggerFactory.getLogger(RestValueTable.class);

  private final TableDto tableDto;

  private final URI tableReference;

  private Timestamps tableTimestamps;

  private final Map<String, Timestamps> valueSetsTimestamps = Maps.newHashMap();

  private boolean valueSetsTimestampsSupported = true;

  RestValueTable(Datasource datasource, TableDto dto) {
    super(datasource, dto.getName());
    tableDto = dto;
    tableReference = getDatasource().newReference("table", dto.getName());
  }

  @NotNull
  @Override
  public RestDatasource getDatasource() {
    return (RestDatasource) super.getDatasource();
  }

  @Override
  public void initialise() {
    super.initialise();
    FederatedVariableEntityProvider provider = new FederatedVariableEntityProvider();
    provider.initialise();
    setVariableEntityProvider(provider);

    Iterable<VariableDto> variables = getOpalClient()
        .getResources(VariableDto.class, newReference("variables"), VariableDto.newBuilder());
    for(final VariableDto dto : variables) {
      addVariableValueSource(new RestVariableValueSource(dto));
    }
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    if(!hasValueSet(entity)) {
      throw new NoSuchValueSetException(this, entity);
    }

    return new ValueSetBean(this, entity);
  }

  @Override
  public boolean canDropValueSets() {
    return true;
  }

  @Override
  public void dropValueSets() {
    try {
      getOpalClient().delete(newReference("valueSets"));
      refresh();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    if(valueSetsTimestampsSupported && valueSetsTimestamps.isEmpty()) {
      initialiseValueSetsTimestamps();
    }

    return valueSetsTimestampsSupported
        ? valueSetsTimestamps.get(entity.getIdentifier())
        : super.getValueSetTimestamps(entity);
  }

  public TableDto getTableDto() {
    return tableDto;
  }

  public Search.QueryResultDto getFacets(Search.QueryTermsDto dtoQueries) {
    return getOpalClient().postResource(Search.QueryResultDto.class, newReference("facets", "_search"),
        Search.QueryResultDto.newBuilder(), dtoQueries);
  }

  private void initialiseValueSetsTimestamps() {
    try {
      ValueSetsDto vss = getOpalClient()
          .getResource(ValueSetsDto.class, newUri("valueSets", "timestamps").query("limit", "-1").build(),
              ValueSetsDto.newBuilder());
      if(vss.getValueSetsCount() > 0) {
        for(ValueSetsDto.ValueSetDto vs : vss.getValueSetsList()) {
          valueSetsTimestamps.put(vs.getIdentifier(), new ValueSetTimestamps(vs.getTimestamps()));
        }
      }
    } catch(Exception e) {
      valueSetsTimestampsSupported = false;
    }
  }

  void refresh() {
    clearSources();
    initialise();
  }

  OpalJavaClient getOpalClient() {
    return getDatasource().getOpalClient();
  }

  URI newReference(String... segments) {
    return getDatasource().buildURI(tableReference, segments);
  }

  UriBuilder newUri(String... segments) {
    return getDatasource().uriBuilder(tableReference).segment(segments);
  }

  private class FederatedVariableEntityProvider implements VariableEntityProvider, Initialisable {

    Iterable<VariableEntityDto> entities;

    @Override
    public String getEntityType() {
      return tableDto.getEntityType();
    }

    @Override
    public void initialise() {
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      ensureEntities();

      return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntityDto, VariableEntity>() {

        @Override
        public VariableEntity apply(VariableEntityDto from) {
          return new VariableEntityBean(getEntityType(), from.getIdentifier());
        }
      }));
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

    private void ensureEntities() {
      if (entities == null) {
        entities = getOpalClient()
            .getResources(VariableEntityDto.class, newReference("entities"), VariableEntityDto.newBuilder());
      }
    }
  }

  private class LazyValueSet {

    private ValueSetsDto valueSet;

    private Timestamps timestamps;

    private VariableEntity variableEntity;

    private LazyValueSet(VariableEntity variableEntity) {
      this.variableEntity = variableEntity;
    }

    public Value get(Variable variable) {
      loadValueSet();
      ValueSetsDto.ValueSetDto values = valueSet.getValueSets(0);

      for(int i = 0; i < valueSet.getVariablesCount(); i++) {
        if(variable.getName().equals(valueSet.getVariables(i))) {
          if(variable.getValueType().equals(BinaryType.get())) {
            return getBinary(variable, values.getValues(i));
          } else {
            return Dtos.fromDto(values.getValues(i), variable.getValueType(), variable.isRepeatable());
          }
        }
      }
      throw new NoSuchVariableException(variable.getName());
    }

    private Value getBinary(Variable variable, ValueSetsDto.ValueDto valueDto) {
      if(variable.isRepeatable()) return getRepeatableBinary(valueDto);
      return getBinaryResource(valueDto);
    }

    private Value getRepeatableBinary(ValueSetsDto.ValueDto valueDto) {
      if(valueDto.getValuesCount() == 0) return BinaryType.get().nullSequence();
      return BinaryType.get().sequenceOf(ImmutableList
          .copyOf(Iterables.transform(valueDto.getValuesList(), new Function<ValueSetsDto.ValueDto, Value>() {

            @Override
            public Value apply(ValueSetsDto.ValueDto input) {
              return getBinaryResource(input);
            }
          })));
    }

    /**
     * Get the binary value directly from the link provided in the value dto.
     *
     * @param valueDto
     * @return
     */
    private Value getBinaryResource(ValueSetsDto.ValueDto valueDto) {
      if(!valueDto.hasLength() || valueDto.getLength() == 0 || !valueDto.hasLink()) return BinaryType.get().nullValue();

      URI uri = getOpalClient().newUri().link(valueDto.getLink()).build();

      InputStream is = null;
      try {
        HttpResponse response = getOpalClient().get(uri);
        if(response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
          EntityUtils.consume(response.getEntity());
          throw new RuntimeException(response.getStatusLine().getReasonPhrase());
        }
        is = response.getEntity().getContent();
        return BinaryType.get().valueOf(ByteStreams.toByteArray(is));
      } catch(IOException e) {
        throw new RuntimeException(e);
      } finally {
        getOpalClient().closeQuietly(is);
      }
    }


    public Timestamps getTimestamps() {
      if(timestamps != null) return timestamps;
      loadTimestamps();
      return timestamps;
    }

    synchronized private void loadTimestamps() {
      try {
        Magma.TimestampsDto tsDto = getOpalClient().getResource(Magma.TimestampsDto.class,
            newUri("valueSet", variableEntity.getIdentifier(), "timestamps").build(),
            Magma.TimestampsDto.newBuilder());
        timestamps = new ValueSetTimestamps(tsDto);
      } catch(RuntimeException e) {
        // legacy with older opals: fallback to table timestamps
        timestamps = RestValueTable.this.getTimestamps();
      }
    }

    synchronized ValueSetsDto loadValueSet() {
      if(valueSet == null) {
        valueSet = getOpalClient().getResource(ValueSetsDto.class,
            newUri("valueSet", variableEntity.getIdentifier()).query("filterBinary", "true").build(),
            ValueSetsDto.newBuilder());
        timestamps = new ValueSetTimestamps(valueSet.getValueSets(0).getTimestamps());
      }
      return valueSet;
    }
  }

  private class ValueSetTimestamps implements Timestamps {

    private final Magma.TimestampsDto tsDto;

    private ValueSetTimestamps(Magma.TimestampsDto tsDto) {
      this.tsDto = tsDto;
    }

    @NotNull
    @Override
    public Value getLastUpdate() {
      if(tsDto != null && tsDto.hasLastUpdate()) {
        return DateTimeType.get().valueOf(tsDto.getLastUpdate());
      }
      return getTimestamps().getLastUpdate();
    }

    @NotNull
    @Override
    public Value getCreated() {
      if(tsDto != null && tsDto.hasCreated()) {
        return DateTimeType.get().valueOf(tsDto.getCreated());
      }
      return getTimestamps().getCreated();
    }
  }

  @Override
  public Timestamps getTimestamps() {
    if(tableTimestamps == null) {
      final Magma.TimestampsDto tsDto = tableDto.getTimestamps();
      tableTimestamps = new Timestamps() {

        @NotNull
        @Override
        public Value getLastUpdate() {
          if(tsDto.hasLastUpdate()) {
            return DateTimeType.get().valueOf(tsDto.getLastUpdate());
          }
          return DateTimeType.get().nullValue();
        }

        @NotNull
        @Override
        public Value getCreated() {
          if(tsDto.hasCreated()) {
            return DateTimeType.get().valueOf(tsDto.getCreated());
          }
          return DateTimeType.get().nullValue();
        }
      };
    }
    return tableTimestamps;
  }

  public class RestVariableValueSource extends AbstractVariableValueSource {

    private final Variable variable;

    private final VariableDto dto;

    private RestVariableValueSource(VariableDto dto) {
      this.dto = dto;
      variable = Dtos.fromDto(dto);
    }

    @Override
    public Variable getVariable() {
      return variable;
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      return new LazyValueSet(valueSet.getVariableEntity()).get(variable);
    }

    @Override
    public boolean supportVectorSource() {
      return false;
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public VectorSource asVectorSource() {
      throw new VectorSourceNotSupportedException((Class<? extends ValueSource>) getClass());
    }

    public VariableDto getVariableDto() {
      return dto;
    }

    public Math.SummaryStatisticsDto getSummary() {
      return getOpalClient()
          .getResource(Math.SummaryStatisticsDto.class, newReference("variable", variable.getName(), "summary"),
              Math.SummaryStatisticsDto.newBuilder());
    }

    public Search.QueryResultDto getFacet() {
      return getOpalClient()
          .getResource(Search.QueryResultDto.class, newReference("facet", "variable", variable.getName(), "_search"),
              Search.QueryResultDto.newBuilder());
    }

  }
}
