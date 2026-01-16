export function toMaxDecimals(x: number | null, n: number): number | null {
  if (x === null) {
    return null;
  }
  return +x.toFixed(n);
}

export function formatNumber(x: number | null, locale: string | undefined = undefined): string {
  return x?.toLocaleString(locale, { maximumFractionDigits: 2 }) ?? '';
}
