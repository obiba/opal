/**
 * Add types (that are not auto-magically added by Quasar CLI already)
 * for your custom variables to avoid TypeScript errors, like dynamic
 * import.meta.env variables or definitions in dotenv files configured
 * ONLY for the /quasar.config file itself.
 *
 * @example
 * interface ImportMetaEnv {
 *   readonly MY_VAR: string;
 * }
 */
// eslint-disable-next-line @typescript-eslint/no-empty-object-type
interface ImportMetaEnv {}
