import { getCurrentLocale } from 'src/boot/i18n';
import { parseISO, formatDistanceStrict, formatDuration, millisecondsToHours, millisecondsToMinutes, millisecondsToSeconds } from 'date-fns';

import { enUS as en } from 'date-fns/locale/en-US';
import { fr } from 'date-fns/locale/fr';

export function getDate(date: string | number | undefined) {
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

export function getMillisLabel(millis: number, compact = true) {
  let value = millis;
  const hours = millisecondsToHours(value);
  value = value - hours * 3600000;
  const minutes = millisecondsToMinutes(value);
  value = value - minutes * 60000;
  const seconds = millisecondsToSeconds(value);
  const milliseconds = value - seconds * 1000;

  if (compact) {
    const labels = [];
    if (hours > 0) labels.push(`${hours} h`);
    if (minutes>0) labels.push(`${minutes} min`);
    if (seconds>0) labels.push(`${seconds} s`);
    if (milliseconds>0) labels.push(`${milliseconds} ms`);
    return labels.join(', ');
  }

  const locale = getCurrentLocale();
  const label = formatDuration({
    hours,
    minutes,
    seconds
  }, {locale: locale === 'fr' ? fr : en,});
  return `${label}${milliseconds>0 ? ' ' + milliseconds + ' ms' : ''}`;
}