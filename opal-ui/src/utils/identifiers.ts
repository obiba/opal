export function generateIdentifier(count: number, allowZeros: boolean, withChecksum: boolean, prefix: string) {
  let sample: string = '9'.repeat(count);

  if (allowZeros) {
    sample = '0' + '9'.repeat(count - 1);
  } else if (withChecksum) {
    sample = '' + '9'.repeat(count - 1) + generateLuhnCheckDigit(parseInt(sample));
  }

  if (!!prefix) {
    sample = `${prefix}${sample}`;
  }

  return sample;
}

export function generateLuhnCheckDigit(input: number): number {
  const str = `${input}`;
  const ints = [];
  for (let i = 0; i < str.length; i++) {
    ints.push(parseInt(str.substring(i, i + 1)));
  }

  for (let i = ints.length - 2; i >= 0; i = i - 2) {
    let j: number = ints[i];
    j = j * 2;
    if (j > 9) {
      j = (j % 10) + 1;
    }
    ints[i] = j;
  }
  let sum = 0;
  for (let i = 0; i < ints.length; i++) {
    sum += ints[i];
  }
  if (sum % 10 == 0) {
    return 0;
  } else return 10 - (sum % 10);
}
