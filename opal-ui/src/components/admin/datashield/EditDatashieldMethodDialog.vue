<template>
  <q-dialog v-model="showDialog" @hide="onHide">
    <q-card class="dialog-sm">
      <q-card-section>
        <div class="text-h6">{{ t(isCreation ? 'add_method' : 'update_method') }}</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-input
          v-model="name"
          :label="t('name')"
          :hint="t('datashield.method_name_hint')"
          :disable="!isCreation"
          dense
          class="q-mb-md"
        />
        <q-select v-model="type" :label="t('type')" :options="typeOptions" dense class="q-mb-md" />
        <q-input
          v-if="type.value === 'r_func'"
          v-model="func"
          :label="t('function')"
          :hint="t('datashield.method_func_hint')"
          placeholder="package::function"
          dense
          class="q-mb-md"
        />
        <q-input
          v-if="type.value === 'r_script'"
          v-model="script"
          :label="t('script')"
          :hint="t('datashield.method_script_hint')"
          placeholder="function(x) { return(x) }"
          dense
          type="textarea"
          class="q-mb-md"
        />
      </q-card-section>
      <q-separator />
      <q-card-actions align="right" class="bg-grey-3">
        <q-btn flat :label="t('cancel')" color="secondary" v-close-popup />
        <q-btn
          flat
          :label="t(isCreation ? 'add' : 'update_action')"
          :disable="
            name.length === 0 ||
            (type.value === 'r_func' && func.length === 0) ||
            (type.value === 'r_script' && script.length === 0)
          "
          color="primary"
          @click="onSubmit"
          v-close-popup
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import type { DataShieldMethodDto, RScriptDataShieldMethodDto, RFunctionDataShieldMethodDto } from 'src/models/DataShield';

interface DialogProps {
  modelValue: boolean;
  env: string;
  method: DataShieldMethodDto | null;
}

const props = defineProps<DialogProps>();
const showDialog = ref(props.modelValue);
const emit = defineEmits(['update:modelValue']);

const datashieldStore = useDatashieldStore();
const { t } = useI18n();

interface MethodTypeOption {
  label: string;
  value: string;
}

const RFuncOption: MethodTypeOption = {
  label: t('r_func'),
  value: 'r_func'
}
const RScriptOption: MethodTypeOption = {
  label: t('r_script'),
  value: 'r_script'
}
const typeOptions: MethodTypeOption[] = [RFuncOption, RScriptOption];

const name = ref('');
const type = ref<MethodTypeOption>(RFuncOption);
const func = ref('');
const script = ref('');

const isCreation = computed(() => props.method === null);

watch(
  () => props.modelValue,
  (value) => {
    showDialog.value = value;
    if (value) {
      name.value = props.method ? props.method.name : '';
      type.value = props.method ? getType(props.method) : RFuncOption;
      func.value = props.method && type.value.value === 'r_func' ? getCode(props.method) : '';
      script.value = props.method && type.value.value === 'r_script' ? getCode(props.method) : '';
    }
  }
);

function onHide() {
  emit('update:modelValue', false);
}

function getType(method: DataShieldMethodDto): MethodTypeOption {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const val = (method as any)['DataShield.RFunctionDataShieldMethodDto.method'] ? 'r_func' : 'r_script';
  return typeOptions.find((o) => o.value === val) || RFuncOption;
}

function getCode(method: DataShieldMethodDto): string {
  if (getType(method).value === 'r_func') {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const funcMethod = (method as any)[
      'DataShield.RFunctionDataShieldMethodDto.method'
    ] as RFunctionDataShieldMethodDto;
    return funcMethod.func;
  } else {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const scriptMethod = (method as any)['DataShield.RScriptDataShieldMethodDto.method'] as RScriptDataShieldMethodDto;
    return scriptMethod.script;
  }
}

function onSubmit() {
  const payload = {
    name: name.value,
  };
  if (type.value.value === 'r_func') {  
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (payload as any)['DataShield.RFunctionDataShieldMethodDto.method'] = {
      func: func.value,
    } as RFunctionDataShieldMethodDto;
  } else {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (payload as any)['DataShield.RScriptDataShieldMethodDto.method'] = {
      script: script.value,
    } as RScriptDataShieldMethodDto;
  }
  if (isCreation.value) {
    datashieldStore.addMethod(props.env, payload as DataShieldMethodDto);
  } else {
    datashieldStore.updateMethod(props.env, payload as DataShieldMethodDto);
  }
}
</script>
