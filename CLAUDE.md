# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Opal is OBiBa's core database application for biobanks and epidemiological studies. 
It provides data storage, management, and harmonization capabilities with R integration for statistical analysis and DataSHIELD support for federated analysis.

## Technology Stack

- **Backend**: Java 25, Spring Framework 6.x, Maven multi-module project
- **Frontend**: Vue 3 + TypeScript with Quasar Framework (in `opal-ui/`)
- **Databases**: MongoDB, MySQL/MariaDB, PostgreSQL, OrientDB (configuration store)
- **Security**: Apache Shiro with OIDC support
- **REST API**: RESTEasy (JAX-RS)
- **R Integration**: Rserve for R server communication
- **Search**: Apache Lucene 9.x

## Build Commands

```bash
# Full build (clean, compile, package)
make all

# Compile only
make compile

# Faster compile (skip GWT - legacy)
mvn install -Dgwt.compiler.skip=true

# Clean
make clean

# Prepare local development environment (run once)
make prepare

# Run Opal server
make run

# Run in debug mode (remote debugging enabled)
make debug
```

## Frontend Development (opal-ui)

```bash
cd opal-ui

# Install dependencies
npm install

# Development server with hot reload (runs on localhost:9000)
npm run dev

# Lint
npm run lint

# Format code
npm run format

# Production build
npm run build
```

**CORS for development**: Add `cors.allowed=http://localhost:9000` to `OPAL_HOME/conf/opal-config.properties`.

## Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific module
cd opal-core && mvn test

# Skip tests during build
mvn install -DskipTests
```

## Module Architecture

| Module | Purpose |
|--------|---------|
| `opal-core-api` | Core domain interfaces and DTOs |
| `opal-core` | Core business logic implementation |
| `opal-core-ws` | Core REST web services |
| `opal-ws` | Additional REST endpoints |
| `opal-spi` | Service Provider Interface for plugins |
| `opal-spi-r` | R-specific SPI |
| `opal-r` | R server integration (Rserve) |
| `opal-datashield` | DataSHIELD federated analysis support |
| `opal-search` | Lucene-based search functionality |
| `opal-fs` | File system management (VFS) |
| `opal-shell` | Command-line shell interface |
| `opal-sql` | SQL parsing (ANTLR-based) |
| `opal-httpd` | HTTP server (Jetty) configuration |
| `opal-server` | Server assembly and startup |
| `opal-rest-client` | Java REST client library |
| `opal-web-model` | Protobuf message definitions |
| `opal-upgrade` | Database migration scripts |
| `opal-ui` | Vue.js admin web application |

## Key External Dependencies

- **Magma**: OBiBa's data abstraction layer (`org.obiba.magma:*`) - handles data sources, variables, and values
- **obiba-commons**: Shared OBiBa utilities including security, OIDC, and Git integration

## Development Setup

1. Ensure Java 25 and Maven 3+ are installed
2. Have MongoDB or MySQL/MariaDB running locally
3. Run `make all` to build
4. Run `make prepare` to initialize `opal_home` directory (first time only)
5. Run `make debug` to start server with remote debugging on port 8000
6. Access at http://localhost:8080 (credentials: administrator/password)

## Environment

- `OPAL_HOME`: Configuration directory (defaults to `./opal_home`)
- Default ports: HTTP 8080, HTTPS 8443, SSH 8022
