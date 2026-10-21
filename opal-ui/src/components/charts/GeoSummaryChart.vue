<template>
  <div>
    <default-summary-chart :variable="variable" :data="data" />
    <div v-if="data.centroid || data.cells?.length" class="row q-col-gutter-md q-mt-md">
      <div class="col-md-8 col-xs-12">
        <vue-plotly :data="mapData" :layout="layout" :config="config" />
        <div v-if="data.cells?.length" class="text-hint q-mt-sm">
          {{ t('geo_summary.h3_cells') }}: {{ t('geo_summary.h3_cells_hint') }}
        </div>
      </div>
      <div v-if="data.centroid" class="col-md-4 col-xs-12">
        <q-markup-table flat bordered dense>
          <thead>
            <tr>
              <th></th>
              <th class="text-right">{{ t('geo_summary.latitude') }}</th>
              <th class="text-right">{{ t('geo_summary.longitude') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>{{ t('geo_summary.bounding_box') }} (min)</td>
              <td class="text-right">{{ format(data.min?.lat) }}</td>
              <td class="text-right">{{ format(data.min?.lon) }}</td>
            </tr>
            <tr>
              <td>{{ t('geo_summary.bounding_box') }} (max)</td>
              <td class="text-right">{{ format(data.max?.lat) }}</td>
              <td class="text-right">{{ format(data.max?.lon) }}</td>
            </tr>
            <tr>
              <td>{{ t('geo_summary.centroid') }}</td>
              <td class="text-right">{{ format(data.centroid?.lat) }}</td>
              <td class="text-right">{{ format(data.centroid?.lon) }}</td>
            </tr>
          </tbody>
        </q-markup-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { VariableDto } from 'src/models/Magma';
import type { GeoSummaryDto } from 'src/models/Math';
import DefaultSummaryChart from 'src/components/charts/DefaultSummaryChart.vue';
import VuePlotly from 'src/components/charts/VuePlotly.vue';

const { t } = useI18n();

interface Props {
  variable: VariableDto;
  data: GeoSummaryDto;
}

const props = defineProps<Props>();

const config = {
  displayModeBar: false,
  responsive: true,
  scrollZoom: true,
};

const mapData = computed(() => {
  const { min, max, centroid, cells } = props.data;
  const traces: object[] = [];
  if (cells?.length) {
    traces.push({
      type: 'choroplethmap',
      geojson: {
        type: 'FeatureCollection',
        features: cells.map((cell) => {
          const ring = cell.boundary.map((p) => [p.lon, p.lat]);
          return {
            type: 'Feature',
            id: cell.index,
            geometry: { type: 'Polygon', coordinates: [[...ring, ring[0]]] },
          };
        }),
      },
      locations: cells.map((cell) => cell.index),
      z: cells.map((cell) => cell.count),
      colorscale: 'Viridis',
      marker: { opacity: 0.6, line: { width: 0 } },
      hovertemplate: '%{z}<extra></extra>',
    });
  }
  // the bounding box and centroid are not sent to users who cannot see the values
  if (!min || !max || !centroid) return traces;
  traces.push({
    type: 'scattermap',
    mode: 'lines',
    lon: [min.lon, max.lon, max.lon, min.lon, min.lon],
    lat: [min.lat, min.lat, max.lat, max.lat, min.lat],
    line: { color: '#e65100', width: 2 },
    name: t('geo_summary.bounding_box'),
    hoverinfo: 'name',
  });
  traces.push({
    type: 'scattermap',
    mode: 'markers',
    lon: [centroid.lon],
    lat: [centroid.lat],
    marker: { size: 12, color: '#e65100' },
    name: t('geo_summary.centroid'),
    hovertemplate: `${t('geo_summary.centroid')}: %{lat:.5f}, %{lon:.5f}<extra></extra>`,
  });
  return traces;
});

// bounding box, or the extent of the cells when it is not sent
const extent = computed(() => {
  const { min, max, cells } = props.data;
  if (min && max) return { min, max };
  const points = (cells ?? []).flatMap((cell) => cell.boundary);
  const lats = points.map((p) => p.lat);
  const lons = points.map((p) => p.lon);
  return {
    min: { lat: Math.min(...lats), lon: Math.min(...lons) },
    max: { lat: Math.max(...lats), lon: Math.max(...lons) },
  };
});

const layout = computed(() => {
  const { min, max } = extent.value;
  // ponytail: rough zoom to fit the bounding box, ignores the latitude distortion
  const span = Math.max(max.lon - min.lon, (max.lat - min.lat) * 2);
  const zoom = span > 0 ? Math.min(12, Math.max(0, Math.log2(360 / span) - 0.5)) : 10;
  return {
    height: 500,
    margin: { l: 0, r: 0, t: 0, b: 0 },
    showlegend: false,
    map: {
      style: 'carto-positron',
      center: props.data.centroid ?? { lat: (min.lat + max.lat) / 2, lon: (min.lon + max.lon) / 2 },
      zoom,
    },
  };
});

function format(value: number | undefined) {
  return value === undefined ? '-' : value.toFixed(5);
}
</script>
