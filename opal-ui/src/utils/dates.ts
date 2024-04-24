import { getCurrentLocale } from 'src/boot/i18n';
import { parseISO } from 'date-fns';

export function getDateLabel(date: string | number | undefined) {
  const locale = getCurrentLocale();
  if (typeof date === 'string') {
    return parseISO(date).toLocaleString(locale);
  } else if (date === undefined || isNaN(date)) {
    return '-';
  }
  return new Date(date).toLocaleString(locale);
}
