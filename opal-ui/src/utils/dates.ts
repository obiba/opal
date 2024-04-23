import { useCookies } from 'vue3-cookies';
import { parseISO } from 'date-fns';

const { cookies } = useCookies();


export function getDateLabel(date: string | number | undefined) {
  const locale = cookies.get('locale');
  if (typeof date === 'string') {
    return parseISO(date).toLocaleString(locale);
  } else if (date === undefined || isNaN(date)) {
    return '-';
  }
  return new Date(date).toLocaleString(locale);
}
