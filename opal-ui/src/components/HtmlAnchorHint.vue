<template>
  <div v-html="htmlText" @click="onClick"></div>
</template>

<script lang="ts">
defineComponent({
  name: 'HtmlAnchorHint',
});
</script>

<script setup lang="ts">
interface Props {
  trKey: string;
  text: string;
  url: string;
}

const props = defineProps<Props>();
const { t } = useI18n();
const htmlText = computed(() => t(props.trKey, { url: `<a href="${props.url}" target="_blank">${props.text}</a>` }));

function onClick() {
  window.open(props.url, '_blank');
}

// onMounted(() => {
//   htmlText.value = t(props.trKey, { url: `<a href="${props.url}" target="_blank">${props.text}</a>` });
// });
</script>
