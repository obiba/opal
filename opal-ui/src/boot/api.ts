import { boot } from 'quasar/wrappers';
import axios from 'axios';
import type { AxiosInstance, AxiosResponse } from 'axios';

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
    $api: AxiosInstance;
  }
}

// context path detection
const locationContextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 2));

const baseUrl = process.env.API && process.env.API.startsWith('/') ? locationContextPath + process.env.API : process.env.API;
const contextPath = locationContextPath || '/';
const PROFILE_PATH = '/system/subject-profile/_current';
const SAFE_PATHS = [PROFILE_PATH, '/auth/session', '/system/conf/general', '/resource-providers'];

const api = axios.create({
  baseURL: baseUrl || '/ws',
  withCredentials: true,
});

function requiresCode(response: AxiosResponse): boolean {
  if (response && response.status === 401) return ['X-Opal-TOTP', 'X-Obiba-TOTP'].includes(response.headers['www-authenticate']);
  return false;
}

api.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    return response;
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    if (
      error.response &&
      !requiresCode(error.response) &&
      [401, 403, 404].includes(error.response.status) &&
      !SAFE_PATHS.includes(error.config.url)
    ) {
      // verify that user is still logged in
      console.debug('error', error);
      api.get(PROFILE_PATH).catch(() => {
        // reload to redirect to sign in page (and reset app state)
        window.location.replace(contextPath);
      });
    }
    return Promise.reject(error);
  }
);

export default boot(({ app }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios;
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api;
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API
});

export { api, baseUrl, contextPath };
