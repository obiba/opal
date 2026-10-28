import { defineBoot } from '#q-app';
import axios from 'axios';
import type { AxiosInstance, AxiosResponse } from 'axios';
import { isReAuthError } from 'src/utils/notify';
import { useAuthStore } from 'src/stores/auth';

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
    $api: AxiosInstance;
  }
}

// context path detection
let locationContextPath = window.location.pathname;
if (locationContextPath.endsWith('/')) {
  locationContextPath = locationContextPath.substring(0, locationContextPath.length - 1);
}

const baseUrl =
  import.meta.env.API && import.meta.env.API.startsWith('/')
    ? locationContextPath + import.meta.env.API
    : import.meta.env.API;
const contextPath = locationContextPath || '/';
const PROFILE_PATH = '/system/subject-profile/_current';
const SAFE_PATHS = [PROFILE_PATH, '/auth/session', '/system/conf/general', '/resource-providers'];

const api = axios.create({
  baseURL: baseUrl || '/ws',
  withCredentials: true,
});

function requiresCode(response: AxiosResponse): boolean {
  if (response && response.status === 401)
    return ['X-Opal-TOTP', 'X-Obiba-TOTP'].includes(response.headers['www-authenticate']);
  return false;
}

// Axios automatically reads XSRF-TOKEN cookie and sends as X-XSRF-TOKEN header
api.defaults.xsrfCookieName = 'XSRF-TOKEN';
api.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';

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
      api
        .get(PROFILE_PATH)
        .then(() => {
          // user is still logged in
          if (isReAuthError(error)) {
            const authStore = useAuthStore();
            authStore.reAuthRequired = true;
          }
        })
        .catch(() => {
          // reload to redirect to sign in page (and reset app state)
          window.location.replace(contextPath);
        });
    }
    return Promise.reject(error);
  },
);

export default defineBoot(({ app }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios;
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api;
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API
});

/** Tagged template for request paths: every interpolated value is percent-encoded as one path segment. */
function u(strings: TemplateStringsArray, ...values: unknown[]): string {
  return strings.reduce(
    (out, str, i) => out + str + (i < values.length ? encodeURIComponent(String(values[i])) : ''),
    '',
  );
}

/** Percent-encodes each segment of a path that already holds slashes, such as a file path or a permission resource. */
function encodePath(path: string): string {
  return path.split('/').map(encodeURIComponent).join('/');
}

/** Decodes a link as served by Opal; a segment that is not valid percent-encoding is kept as is. */
function decodePath(path: string): string {
  return path
    .split('/')
    .map((s) => {
      try {
        return decodeURIComponent(s);
      } catch {
        return s;
      }
    })
    .join('/');
}

export { api, baseUrl, contextPath, u, encodePath, decodePath };
