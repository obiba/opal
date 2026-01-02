<template>
  <q-layout>
    <q-page-container>
        <q-page class="q-pa-none">
            <!-- ReDoc mounts here -->
            <div id="redoc-container" style="height: 100vh;"></div>
        </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { baseUrl } from 'src/boot/api';

declare global {
  interface Window {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    Redoc: any
  }
}

onMounted(() => {
  const redocConfig = {}
  // Avoid loading multiple times if user navigates back
  if (window.Redoc) {
    window.Redoc.init(`${baseUrl}/openapi.json`, redocConfig, document.getElementById('redoc-container'))
    return
  }

  const script = document.createElement('script')
  script.src = 'https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js'
  script.onload = () => {
    window.Redoc.init(
      `${baseUrl}/openapi.json`,
      redocConfig,
      document.getElementById('redoc-container')
    )
  }

  document.head.appendChild(script)
})
</script>
