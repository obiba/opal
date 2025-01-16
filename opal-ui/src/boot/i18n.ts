import { boot } from 'quasar/wrappers';
import { createI18n } from 'vue-i18n';
import messages from 'src/i18n';
import { Quasar, Cookies } from 'quasar'
export type MessageLanguages = keyof typeof messages;
// Type-define 'en-US' as the master schema for the resource
export type MessageSchema = typeof messages['en'];
// See https://vue-i18n.intlify.dev/guide/advanced/typescript.html#global-resource-schema-type-definition
/* eslint-disable @typescript-eslint/no-empty-object-type */
declare module 'vue-i18n' {
  // define the locale messages schema
  export interface DefineLocaleMessage extends MessageSchema { }
  // define the datetime format schema
  export interface DefineDateTimeFormat { }
  // define the number format schema
  export interface DefineNumberFormat { }
}
/* eslint-enable @typescript-eslint/no-empty-object-type */

const defaultLocales = ['en', 'fr'];

const locales = defaultLocales;

function getCurrentLocale(): string {
  let detectedLocale = Cookies.get('locale')
    ? Cookies.get('locale') // previously selected
    : Quasar.lang.getLocale(); // browser
  if (!detectedLocale) {
    detectedLocale = locales[0];
  } else if (!locales.includes(detectedLocale)) {
    detectedLocale = detectedLocale.split('-')[0];
    if (!detectedLocale || !locales.includes(detectedLocale)) {
      detectedLocale = locales[0];
    }
  }
  return detectedLocale || locales[0] || 'en';
}

const i18n = createI18n<{ message: MessageSchema }, MessageLanguages>({
  locale: getCurrentLocale(),
  fallbackLocale: locales[0] || 'en',
  globalInjection: true,
  legacy: false,
  messages,
});

export default boot(({ app }) => {
  // Set i18n instance on app
  app.use(i18n);
});

export { i18n, locales, getCurrentLocale };
