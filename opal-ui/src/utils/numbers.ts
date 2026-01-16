export function toMaxDecimals(x: number | null, n: number): number | null {
  if (x === null) {
    return null;
  }
  return +x.toFixed(n);
}

export function formatNumber(x: number | null, locale: string | undefined = undefined): string {
  if (x === null) {
    return '';
  }
  if (Number.isInteger(x)) {
    return x.toLocaleString(locale);
  }
  return x.toLocaleString(locale, { maximumFractionDigits: 2 });
}
