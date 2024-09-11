import { getCurrentLocale } from 'src/boot/i18n';
import { parseISO, formatDistanceStrict } from 'date-fns';

import { enUS as en } from 'date-fns/locale/en-US';
import { fr } from 'date-fns/locale/fr';

function getDate(date: string | number | undefined) {
  if (typeof date === 'string') {
    return parseISO(date);
  } else if (date === undefined || isNaN(date)) {
    return null;
  }
  return new Date(date);
}

export function getDateLabel(date: string | number | undefined) {
  const locale = getCurrentLocale();
  const localeDate = getDate(date);
  return localeDate ? localeDate.toLocaleString(locale) : '-';
}

export function getDateDistanceLabel(date: string | number | undefined) {
  const now = new Date();
  const locale = getCurrentLocale();
  const localeDate = getDate(date);
  return localeDate
    ? formatDistanceStrict(localeDate, now, {
        addSuffix: true,
        locale: locale === 'fr' ? fr : en,
      })
    : '-';
}
