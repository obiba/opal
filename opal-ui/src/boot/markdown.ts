import { useQMarkdownGlobalProps } from '@quasar/quasar-ui-qmarkdown';
import { sanitizePlugin } from 'src/utils/markdown';

// Global props take precedence over the component ones: every q-markdown output gets sanitized.
useQMarkdownGlobalProps({ plugins: [sanitizePlugin] });
