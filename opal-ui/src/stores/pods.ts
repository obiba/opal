import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import type { PodSpecDto } from 'src/models/K8s';

export const usePodsStore = defineStore('pods', () => {
  const podSpecs = ref<PodSpecDto[]>([]);

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
      return response.data;
    });
  }

  async function savePodSpec(podSpec: PodSpecDto) {
    return api.post('/pod-specs', podSpec).then(loadPodSpecs);
  }

  async function removePodSpec(id: string) {
    return api.delete(`/pod-spec/${id}`);
  }

  return {
    podSpecs,
    reset,
    initPodSpecs,
    removePodSpec,
    savePodSpec,
  };
});
