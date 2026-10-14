# City Tour Planner

## Overview

City Tour Planner is a software design course project that aims to generate a simple city tour by combining data from multiple independent external data sources.

The application will allow users to select a city and a preferred time, retrieve relevant information from external sources, and generate a tour based on the available data. The results will be presented through suitable visualizations to help users explore the city.

## Objectives

- Retrieve data from multiple independent external sources.
- Combine the retrieved data in a meaningful way.
- Generate a basic itinerary based on the available information.
- Present the results through visualizations such as maps, charts, and itinerary views.
- Design the system so that additional data sources can be integrated in the future.

## Initial Scope

The project will initially explore the following capabilities:

- City and date/time selection.
- Weather information using FMI Open Data.
- Relevant transport or traffic information using Digitraffic.
- Places and points of interest using a suitable open-data dataset.
- Basic tour generation using the collected data.
- Visualization of the generated tour and relevant contextual information.

The exact features and supported data sources will be finalized after API research and testing.

## Candidate Data Sources

- **FMI Open Data** — Weather forecasts and observations.
    - Documentation: https://en.ilmatieteenlaitos.fi/open-data-manual
- **Digitraffic** — Transport and traffic-related data.
    - Documentation: https://www.digitraffic.fi/en/
- **Open city data** — Potential datasets for attractions and points of interest.
    - Catalogue: https://www.avoindata.fi/

These are candidate sources only. The team will investigate their available data, response formats, access requirements, limitations, and suitability before deciding which sources to integrate.

## Planned Technology

- **Backend:** Java
- **Frontend:** React, HTML, and CSS
- **External data:** To be determined through API research

The final architecture, libraries, and integration approach will be decided after the initial research phase.

## Development Approach

Development will begin with collaborative research and testing of the candidate APIs and datasets.

The team will:

1. Investigate each candidate data source.
2. Test representative requests and inspect actual responses.
3. Identify useful data fields, access requirements, and limitations.
4. Evaluate whether the sources support the intended application.
5. Agree on the data sources to use.
6. Design the system architecture and begin implementation.

## Team Responsibilities

- **Varun:** Architecture and integration.
- **George:** Tour-generation logic and design rationale.
- **Thanh:** User interface prototype.

The team will collaborate on API research and the decisions that affect the overall system design.

## Project Status

**Current phase:** Repository setup and API research.

Detailed architecture and design documentation will be developed after the team has tested the candidate data sources and agreed on the integration approach.