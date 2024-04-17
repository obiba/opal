export function toMaxDecimals(x: number | null, n: number): number | null {
  if (x === null) {
    return null;
  }
  return +x.toFixed(n);
}
