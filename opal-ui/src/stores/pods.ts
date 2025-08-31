import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PodRefDto, PodSpecDto } from 'src/models/K8s';

export const usePodsStore = defineStore('pods', () => {
  const podSpecs = ref<PodSpecDto[]>([]);
  const podRefs = ref<{[key: string]: PodRefDto[]}>({});

  function reset() {
    podSpecs.value = [];
  }

  async function initPodSpecs() {
    return loadPodSpecs();
  }

  async function loadPodSpecs() {
    podSpecs.value = [];
    return api.get('/pod-specs').then((response) => {
      podSpecs.value = response.data;
      podSpecs.value.forEach((ps) => {
        getPods(ps.id);
      });
      return response.data;
    });
  }

  async function savePodSpec(podSpec: PodSpecDto) {
    return api.post('/pod-specs', podSpec).then(loadPodSpecs);
  }

  async function removePodSpec(id: string) {
    return api.delete(`/pod-spec/${id}`);
  }

  async function getPods(id: string): Promise<PodRefDto[]> {
    podRefs.value[id] = podRefs.value[id] || [];
    return api.get(`/pod-spec/${id}/pods`).then((response) => {
      podRefs.value[id] = response.data;
      return response.data;
    });
  }

  async function removePods(id: string) {
    return api.delete(`/pod-spec/${id}/pods`);
  }

  async function getPod(id: string, name: string): Promise<PodRefDto | undefined> {
    return api.get(`/pod-spec/${id}/pod/${name}`).then((response) => {
      return response.data;
    });
  }

  async function removePod(id: string, name: string) {
    return api.delete(`/pod-spec/${id}/pod/${name}`);
  }

  return {
    podSpecs,
    podRefs,
    reset,
    initPodSpecs,
    removePodSpec,
    savePodSpec,
    getPods,
    removePods,
    getPod,
    removePod,
  };
});
