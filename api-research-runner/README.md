# API Research Runner

## Overview

The API Research Runner is a command-line tool developed by **Varun Ganesh Sondur** to test external APIs, evaluate their availability, measure response times, and inspect their responses.

It automates API testing using configurable endpoints and generates a JSON report summarizing the results.

## Features

- Tests configured API endpoints automatically.
- Records HTTP status codes, response times, and content types.
- Captures response previews and error details.
- Supports gzip-compressed responses.
- Generates a structured JSON report with test summaries.
- Allows API sources and endpoints to be configured through YAML without changing the Java code.

## Getting Started

### Prerequisites

- Java 22
- Maven
- Internet connection

### Installation

Clone the repository:

```bash
  git clone <repository-url>
```

Navigate to the project directory:

```bash
  cd <repository-directory>/api-research-runner
```

### Run

Execute the application using Maven:

```bash
  mvn spring-boot:run
```

The runner automatically executes the configured API tests and prints a summary to the terminal.

## Configuration

Configure API sources, endpoints, HTTP methods, query parameters, and timeout settings in:

`src/main/resources/application.yml`

Sources can be enabled or disabled individually.

## Output

The test report is generated at:

`reports/api-test-suite-results.json`

It contains the overall test summary and individual endpoint results, including status codes, response times, content types, response previews, and errors where applicable.

The report is overwritten each time the runner executes.

## Author

**Varun Ganesh Sondur**