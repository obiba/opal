export function generateName(prefix: string, names: string[]): string {
  let i = 1;
  let name = prefix + '-' + i;
  while (names.includes(name)) {
    i++;
    name = prefix + '-' + i;
  }
  return name;
}
