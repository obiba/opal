export function generateName(prefix: string, names: string[]): string {
  let i = 1;
  let name = prefix + '-' + i;
  while (names.includes(name)) {
    i++;
    name = prefix + '-' + i;
  }
  return name;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function flattenObjectToString(object: any, icase = true): string {
  let result = '';

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  function recurse(value: string | any) {
    if (Array.isArray(value)) {
      value.forEach((item) => recurse(item));
    } else if (value && typeof value === 'object') {
      for (const key in value) {
        if (value.hasOwnProperty(key)) {
          recurse(value[key]);
        }
      }
    } else {
      result += ` ${value}`;
    }
  }

  recurse(object);

  result = result.trim();
  return icase ? result.toLowerCase() : result;
}

export const includesToken = (source: string, token: string, ignoreCase = true) => {
  if (!source || !token) return true
  return ignoreCase ? source.toLowerCase().includes(token.toLowerCase()) : source.includes(token);
};
