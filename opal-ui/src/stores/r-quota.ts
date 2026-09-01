import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { RQuotaDto, RQuotaUsageDto } from 'src/models/OpalR';

export const useRQuotaStore = defineStore('rQuota', () => {
  const quotas = ref<RQuotaDto[]>([]);

  function reset() {
    quotas.value = [];
  }

  async function initQuotas(context: string) {
    reset();
    return loadQuotas(context);
  }

  async function loadQuotas(context: string) {
    return api.get('/service/r/quotas', { params: { context } }).then((response) => {
      quotas.value = response.data;
    });
  }

  async function addQuota(quota: RQuotaDto) {
    return api.post('/service/r/quotas', quota);
  }

  async function updateQuota(quota: RQuotaDto) {
    return api.put(`/service/r/quota/${quota.id}`, quota);
  }

  async function deleteQuota(quota: RQuotaDto) {
    return api.delete(`/service/r/quota/${quota.id}`);
  }

  /**
   * The quota that applies to a user and what they have spent against it. Reading someone else's requires
   * administration permissions, whereas _current reports on the authenticated user and is open to them.
   */
  async function getUsage(context: string, user: string): Promise<RQuotaUsageDto> {
    return api.get('/service/r/quotas/_usage', { params: { context, user } }).then((response) => response.data);
  }

  async function getCurrentUsage(context: string): Promise<RQuotaUsageDto> {
    return api.get('/service/r/quotas/_current', { params: { context } }).then((response) => response.data);
  }

  return {
    quotas,
    reset,
    initQuotas,
    loadQuotas,
    addQuota,
    updateQuota,
    deleteQuota,
    getUsage,
    getCurrentUsage,
  };
});
