import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RActivitySummaryDto } from 'src/models/OpalR';

export const useProfileActivityStore = defineStore('profileActivity', () => {

  const rSummaries = ref<RActivitySummaryDto[]>([]);
  const datashieldSummaries = ref<RActivitySummaryDto[]>([]);
  const sqlSummaries = ref<RActivitySummaryDto[]>([]);

  const summaries = computed(() => [...rSummaries.value, ...datashieldSummaries.value, ...sqlSummaries.value].sort((a, b) => a.profile.localeCompare(b.profile)))

  function reset() {
    rSummaries.value = [];
    datashieldSummaries.value = [];
    sqlSummaries.value = [];
  }

  async function initSummaries(principal: string) {
    return Promise.all([
      api.get('/service/r/activity/_summary', { params: { context: 'R', user: principal } }).then((resp) => rSummaries.value = resp.data),
      api.get('/service/r/activity/_summary', { params: { context: 'DataSHIELD', user: principal } }).then((resp) => datashieldSummaries.value = resp.data),
      api.get('/service/r/activity/_summary', { params: { context: 'SQL', user: principal } }).then((resp) => sqlSummaries.value = resp.data),
    ]);
  }

  async function getRSessionActivities(principal: string, context: string, profile: string) {
    return api.get('/service/r/activity', { params: { context, profile, user: principal } }).then((resp) => resp.data);
  }

  return {
    rSummaries,
    datashieldSummaries,
    summaries,
    reset,
    initSummaries,
    getRSessionActivities,
  }
});