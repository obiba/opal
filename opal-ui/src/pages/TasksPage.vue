<template>
  <div>
    <q-toolbar class="bg-grey-3">
      <q-breadcrumbs>
        <q-breadcrumbs-el icon="home" to="/" />
        <q-breadcrumbs-el :label="$t('tasks')" />
      </q-breadcrumbs>
    </q-toolbar>
    <q-page class="q-pa-md">
      <command-states
        :commands="commandsStore.commandStates"
        @refresh="onRefresh"
        @clear="onClear" />
    </q-page>
  </div>
</template>

<script setup lang="ts">
import CommandStates from 'src/components/CommandStates.vue';

const commandsStore = useCommandsStore();


onMounted(() => {
  commandsStore.loadCommandStates();
});

function onRefresh() {
  commandsStore.loadCommandStates();
}

function onClear() {
  commandsStore.clearCommandStates().then(() => {
    commandsStore.loadCommandStates();
  });
}
</script>
