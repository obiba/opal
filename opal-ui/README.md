# Opal administration webapp (opal-ui)

An interface to manage Opal content

## Install the dependencies

```bash
npm install
```

### Start the app in development mode (hot-code reloading, error reporting, etc.)

```bash
npm run dev
```

### Lint the files

```bash
npm run lint
```

### Format the files

```bash
npm run format
```

### Build the app for production

```bash
npm run build
```

### Customize the configuration

See [Configuring quasar.config.js](https://v2.quasar.dev/quasar-cli-vite/quasar-config-js).

### CORS Setting during development

Add `cors.allowed=http://localhost:9000` in `OPAL_HOME/config/opal-config.properties` to make sure REST calls do not fail.
