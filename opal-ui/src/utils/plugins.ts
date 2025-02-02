import type { PluginPackagesDto, PluginPackageDto } from 'src/models/Plugins';
import { i18n } from 'src/boot/i18n';

const mergeLocaleMessage = i18n.global.mergeLocaleMessage;

type Translation = {
  [key: string]: string;
};

type Translations = {
  [key: string]: Translation;
};

type PluginTranslations = {
  plugins: {
    [key: string]: Translations;
  };
};

export function mergeAnalysesTranslations(packages: PluginPackagesDto) {
  if (packages) {
    const normalizeFn = (plugin: PluginPackageDto, translations: Translations) => {
      const normalized: PluginTranslations = { plugins: {} } as PluginTranslations;
      normalized.plugins[plugin.name] = translations;
      return normalized;
    };

    packages.packages?.forEach((plugin) => {
      const pluginTranslations: Translations = {};
      pluginTranslations[plugin.name] = { title: plugin.title, description: plugin.description };
      mergeLocaleMessage('en', { plugins: pluginTranslations });

      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const templates = ((plugin as any)['Plugins.AnalysisPluginPackageDto.analysis'] || {}).analysisTemplates || [];
      const tplTranslations: Translations = {};
      templates.forEach((template: PluginPackageDto) => {
        tplTranslations[template.name] = { title: template.title, description: template.description };
        mergeLocaleMessage('en', normalizeFn(plugin, tplTranslations));
      });
    });
  }
}
