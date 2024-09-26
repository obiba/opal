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
  return getDatesDistanceLabel(date, now.getTime());
}

export function getDatesDistanceLabel(date1: string | number | undefined, date2: string | number | undefined, addSuffix = true) {
  const locale = getCurrentLocale();
  const localeDate1 = getDate(date1);
  const localeDate2 = getDate(date2) || new Date();
  return localeDate1
    ? formatDistanceStrict(localeDate1, localeDate2, {
        addSuffix,
        locale: locale === 'fr' ? fr : en,
      })
    : '-';
}

export function getMillisLabel(millis: number) {
  if (millis < 1000)
    return `${millis} ms`;
  else 
    return `${millis/1000} s`;
}