import { defineStore } from 'pinia';
import { api } from 'src/boot/api';
import { Perms } from 'src/utils/authz';
import { type CommandStateDto, CommandStateDto_Status } from 'src/models/Commands';

interface CommandsPerms {
  commands?: Perms;
}

export const useCommandsStore = defineStore('commands', () => {
  const commandStates = ref([] as CommandStateDto[]);
  const perms = ref<CommandsPerms>({});

  function init() {
    perms.value = {};
    api.options('/shell/commands').then((response) => {
      perms.value.commands = new Perms(response);
    });
  }

  function reset() {
    commandStates.value = [];
  }

  function refresh() {
    commandStates.value = [];
    loadCommandStates();
  }

  async function loadCommandStates() {
    return api.get('/shell/commands').then((response) => {
      commandStates.value = response.data;
      return response;
    });
  }

  async function clear(commands: CommandStateDto[]) {
    if (commands.length === 0) {
      return api.delete('/shell/commands/completed').then((response) => {
        commandStates.value = [];
        return response;
      });
    } else {
      return Promise.all(commands.map((cmd) => api.delete(`/shell/command/${cmd.id}`)));
    }
  }

  function cancel(command: CommandStateDto) {
    return api.put(`/shell/command/${command.id}/status`, { status: CommandStateDto_Status.CANCELED });
  }

  return {
    commandStates,
    perms,
    init,
    reset,
    refresh,
    clear,
    cancel,
  };
});
