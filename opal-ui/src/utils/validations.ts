// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function isEmpty(value: any): boolean {
  if (Array.isArray(value)) {
    return value.length === 0;
  } else if (typeof value === 'object') {
    return Object.keys(value).length === 0;
  } else if (typeof value === 'string') {
    return value === undefined || value === null || value === '';
  }

  return value === undefined || value === null;
}