/** Tagged template for request paths: every interpolated value is percent-encoded as one path segment. */
export function u(strings: TemplateStringsArray, ...values: unknown[]): string {
  return strings.reduce(
    (out, str, i) => out + str + (i < values.length ? encodeURIComponent(String(values[i])) : ''),
    '',
  );
}

/** Percent-encodes each segment of a path that already holds slashes, such as a file path or a permission resource. */
export function encodePath(path: string): string {
  return path.split('/').map(encodeURIComponent).join('/');
}

/** Decodes a link as served by Opal; a segment that is not valid percent-encoding is kept as is. */
export function decodePath(path: string): string {
  return path
    .split('/')
    .map((s) => {
      try {
        return decodeURIComponent(s);
      } catch {
        return s;
      }
    })
    .join('/');
}
