import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { CommandStateDto } from 'src/models/Commands';

export const useCommandsStore = defineStore('commands', () => {
  const commandStates = ref([] as CommandStateDto[]);

  function reset() {
    commandStates.value = [];
  }

  async function loadCommandStates() {
    return api.get('/shell/commands').then((response) => {
      commandStates.value = response.data;
      return response;
    });
  }

  async function clearCommandStates() {
    return api.delete('/shell/commands').then((response) => {
      commandStates.value = [];
      return response;
    });
  }

  return {
    commandStates,
    reset,
    loadCommandStates,
    clearCommandStates,
  };
});
