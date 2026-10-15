import { describe, expect, it } from 'vitest';
import markdownIt from 'markdown-it';
import { sanitizeHtml, sanitizePlugin } from './markdown';

// Same options as q-markdown defaults: raw HTML is enabled.
function render(src: string, inline = false): string {
  const md = markdownIt({ html: true, linkify: true, typographer: true, breaks: true });
  md.use(sanitizePlugin);
  return inline ? md.renderInline(src) : md.render(src);
}

const PAYLOADS = [
  '<img src=x onerror=alert(1)>',
  '<IMG SRC=x OnErRoR=alert(1)>',
  '<svg onload=alert(1)>',
  '<svg><animate onbegin=alert(1) attributeName=x dur=1s>',
  '<details open ontoggle=alert(1)>',
  '<body onload=alert(1)>',
  '<script>alert(1)</script>',
  '<iframe src="javascript:alert(1)"></iframe>',
  '<object data="javascript:alert(1)"></object>',
  '<embed src="javascript:alert(1)">',
  '<a href="javascript:alert(1)">click</a>',
  '<a href="JaVaScRiPt:alert(1)">click</a>',
  '<a href="data:text/html,<script>alert(1)</script>">click</a>',
  '<math><mtext><table><mglyph><style><img src=x onerror=alert(1)>',
  '<form action="javascript:alert(1)"><button>go</button></form>',
  '<input autofocus onfocus=alert(1)>',
  '<style>body { display: none }</style>',
  '<div style="background:url(javascript:alert(1))">x</div>',
];

// Quasar classes that would overlay the page with an attacker's content (e.g. a fake login message).
const OVERLAYS = [
  '<div class="fixed-full z-max bg-white text-h3">Session expired, log in at https://evil.example</div>',
  '<span class="fullscreen z-top">x</span>',
  '<p class="absolute-top-left">x</p>',
];

function expectSafe(html: string) {
  expect(html).not.toMatch(/<script/i);
  expect(html).not.toMatch(/<iframe|<object|<embed|<form|<input|<button|<style/i);
  expect(html).not.toMatch(/\son\w+\s*=/i);
  expect(html).not.toMatch(/\sstyle\s*=/i);
  expect(html).not.toMatch(/="\s*javascript:/i);
  expect(html).not.toMatch(/href="data:/i);
}

describe('sanitizeHtml', () => {
  it.each(PAYLOADS)('neutralizes %s', (payload) => {
    expectSafe(sanitizeHtml(payload));
  });

  it('keeps harmless HTML', () => {
    expect(sanitizeHtml('<b>bold</b><br><i>it</i>')).toBe('<b>bold</b><br><i>it</i>');
  });

  it.each(['_blank', 'other'])('forces rel on links with target %s', (target) => {
    const html = sanitizeHtml(`<a href="https://example.org" target="${target}" rel="opener">x</a>`);
    expect(html).toContain(`target="${target}"`);
    expect(html).toContain('rel="noopener noreferrer"');
  });

  it.each(['<th style="position:fixed">x</th>', '<p style="text-align:center">x</p>', '<td style="text-align:center;position:fixed">x</td>'])(
    'removes style other than cell alignment %s',
    (src) => {
      expect(sanitizeHtml(`<table><tr>${src}</tr></table>`)).not.toMatch(/\sstyle\s*=/i);
    },
  );

  it.each(OVERLAYS)('removes application classes %s', (src) => {
    expect(sanitizeHtml(src)).not.toMatch(/\sclass\s*=/i);
  });

  it('keeps q-markdown and code highlighting classes', () => {
    const src =
      '<a class="q-markdown--link q-markdown--link-external" href="https://example.org">x</a>' +
      '<pre class="q-markdown--code language-js"><code><span class="token keyword">if</span></code></pre>';
    expect(sanitizeHtml(src)).toBe(src);
  });

  it('keeps only the q-markdown classes of a mixed class value', () => {
    expect(sanitizeHtml('<div class="q-markdown--note fixed-full z-max">x</div>')).toBe(
      '<div class="q-markdown--note">x</div>',
    );
  });
});

describe('sanitizePlugin', () => {
  it.each(PAYLOADS)('neutralizes %s in block rendering', (payload) => {
    expectSafe(render(payload));
  });

  it.each(PAYLOADS)('neutralizes %s in inline rendering', (payload) => {
    expectSafe(render(payload, true));
  });

  it('neutralizes HTML mixed with markdown', () => {
    const html = render('## Hello\n<img src=x onerror=alert(1)>');
    expect(html).toContain('<h2>Hello</h2>');
    expectSafe(html);
  });

  it.each([
    '![x](https://example.org/x.png)',
    '<img src="https://example.org/x.png">',
    '<svg><image href="https://example.org/x.png"></image></svg>',
    '<video src="https://example.org/x.mp4"></video>',
    '<audio><source src="https://example.org/x.mp3"></audio>',
    '<picture><source srcset="https://example.org/x.png"></picture>',
  ])('removes media loading external resources %s', (src) => {
    expect(render(src)).not.toMatch(/<(img|svg|image|video|audio|source|track|picture)\b/i);
  });

  it.each(OVERLAYS)('removes application classes %s in block rendering', (src) => {
    expect(render(src)).not.toMatch(/\sclass\s*=/i);
  });

  it.each(OVERLAYS)('removes application classes %s in inline rendering', (src) => {
    expect(render(src, true)).not.toMatch(/\sclass\s*=/i);
  });

  it('neutralizes javascript markdown links', () => {
    expectSafe(render('[click](javascript:alert(1))'));
  });

  it('keeps markdown formatting', () => {
    const html = render('# Title\n\n**bold** _it_\n\n- a\n- b\n\n| h |\n| - |\n| c |\n\n[link](https://example.org)');
    expect(html).toContain('<h1>Title</h1>');
    expect(html).toContain('<strong>bold</strong>');
    expect(html).toContain('<em>it</em>');
    expect(html).toContain('<li>a</li>');
    expect(html).toContain('<td>c</td>');
    expect(html).toContain('<a href="https://example.org">link</a>');
  });

  it('keeps table column alignment', () => {
    const html = render('| l | c | r |\n| :- | :-: | -: |\n| a | b | c |');
    expect(html).toContain('<th style="text-align:left">l</th>');
    expect(html).toContain('<td style="text-align:center">b</td>');
    expect(html).toContain('<td style="text-align:right">c</td>');
  });

  it('keeps escaped code content', () => {
    const html = render('```\nif (a < b && c > d) {}\n```');
    expect(html).toContain('if (a &lt; b &amp;&amp; c &gt; d) {}');
  });

  it('keeps inline HTML formatting', () => {
    expect(render('line one<br>**line** <i>two</i>', true)).toBe('line one<br><strong>line</strong> <i>two</i>');
  });
});
