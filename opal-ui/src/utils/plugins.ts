import { plugins } from 'app/postcss.config.cjs';
import { i18n } from 'src/boot/i18n';
import { PluginPackagesDto, PluginPackageDto } from 'src/models/Plugins';

type Translation = {
  [key: string]: string;
};

type Translations = {
  [key:string]: Translation
};

type PluginTranslations = {
  plugins: {
    [key: string]: Translations
  };
}

type TemplateTranslations = {
    [key: string]: {
      title: string;
      description: string;
    }
}

export function addAnalysesTranslations(packages: PluginPackagesDto) {
  if (!!packages) {
    const normalize = (plugin:PluginPackageDto, translations: Translations) => {
      const normalized:PluginTranslations = {plugins: {}} as PluginTranslations;
      normalized.plugins[plugin.name] = translations;
      return normalized;
    };

    ((packages || {}).packages || []).forEach((plugin) => {
      const templates = (plugin['Plugins.AnalysisPluginPackageDto.analysis'] || {}).analysisTemplates || [];
      const tplTranslations: Translations = {};

      templates.forEach((template: PluginPackageDto) => {
        tplTranslations[template.name] = { title: template.title, description: template.description };
        i18n.global.mergeLocaleMessage('en', normalize(plugin, tplTranslations));

        if ('i18n' in template) {
          const templateI18n: TemplateTranslations = template.i18n as TemplateTranslations;
          Object.keys(i18n).forEach((key: string) => {
            const i18nTranslations = {} as Translations;
            tplTranslations[template.name] = { title: templateI18n[key].title, description: templateI18n[key].description };
            i18n.global.mergeLocaleMessage(key, normalize(plugin, i18nTranslations));
          });
        }
      });
    });
  }
}
