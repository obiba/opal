import { boot } from 'quasar/wrappers';
import axios, { AxiosInstance } from 'axios';

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
    $api: AxiosInstance;
  }
}

const baseUrl = process.env.API;
const contextPath = process.env.PATH_PREFIX || '/';
const PROFILE_PATH = '/system/subject-profile/_current';

const api = axios.create({
  baseURL: baseUrl,
  withCredentials: true,
});

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
      [401, 403, 404].includes(error.response.status) &&
      error.config.url !== PROFILE_PATH
    ) {
      // verify that user is still logged in
      console.log('error', error);
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
