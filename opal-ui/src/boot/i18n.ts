import { boot } from 'quasar/wrappers';
import { createI18n } from 'vue-i18n';
import messages from 'src/i18n';
import { Quasar } from 'quasar';
import { useCookies } from 'vue3-cookies';

const { cookies } = useCookies();

const defaultLocales = ['en', 'fr'];

const locales = defaultLocales;

function getCurrentLocale() {
  let detectedLocale = cookies.get('locale')
    ? cookies.get('locale') // previously selected
    : Quasar.lang.getLocale(); // browser
  if (!detectedLocale) {
    detectedLocale = locales[0];
  } else if (!locales.includes(detectedLocale)) {
    detectedLocale = detectedLocale.split('-')[0];
    if (!locales.includes(detectedLocale)) {
      detectedLocale = locales[0];
    }
  }
  return detectedLocale;
}

const i18n = createI18n({
  locale: getCurrentLocale(),
  fallbackLocale: locales[0],
  globalInjection: true,
  legacy: false,
  messages,
});
const t = i18n.global.t;

export default boot(({ app }) => {
  // Set i18n instance on app
  app.use(i18n);
});

export { i18n, t, locales, getCurrentLocale };
