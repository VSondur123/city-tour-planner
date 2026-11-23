
/**
 * Converts the backend TourResult into the format
 * currently expected by the React UI components.
 *
 * The original API response is preserved in `raw`.
 */

function formatTime(isoString) {
  if (!isoString) return '--:--'

  const date = new Date(isoString)

  if (Number.isNaN(date.getTime())) {
    return '--:--'
  }

  return new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Europe/Helsinki',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).format(date)
}

function convertStops(stops = []) {
  return stops.map((stop, index) => ({
    id: index + 1,
    placeId: stop.place.id,
    name: stop.place.name,
    category: stop.place.category,
    indoor: stop.place.indoor,
    lat: stop.place.lat,
    lng: stop.place.lon,
    visitMinutes: stop.place.visitMinutes,
    link: stop.place.link,
    start: stop.start,
    end: stop.end,
    time: `${formatTime(stop.start)}–${formatTime(stop.end)}`,
  }))
}

function convertWeather(forecast = []) {
  return forecast.map((item) => ({
    hour: formatTime(item.time),
    time: item.time,
    temp: item.temperature,
    rain: item.precipitationAmount,
    cloudCover: item.cloudCover,
    wind: item.wind,
  }))
}

function convertTraffic(announcements = []) {
  return announcements.flatMap((announcement, index) => {
    const paths = announcement.geometry?.paths ?? []

    // Each geometry path must be rendered separately.
    // The existing MapView expects GeoJSON-style [lng, lat].
    const lines = paths
      .filter((path) => Array.isArray(path) && path.length >= 2)
      .map((path, pathIndex) => ({
        id: `traffic-${index}-${pathIndex}`,
        type: announcement.type,
        description: announcement.description,
        area: announcement.area,
        language: announcement.language,
        startTime: announcement.startTime,
        endTime: announcement.endTime,
        line: path.map((point) => [point.lon, point.lat]),
      }))

    return lines
  })
}

export function adaptTourResult(apiResult) {
  return {
    stops: convertStops(apiResult.stops ?? []),
    weather: convertWeather(apiResult.forecast ?? []),
    traffic: convertTraffic(apiResult.announcements ?? []),

    // Keep all original announcements for the traffic panel.
    announcements: apiResult.announcements ?? [],

    weatherStatus: apiResult.weatherStatus,
    trafficStatus: apiResult.trafficStatus,
    trafficTextLanguage: apiResult.trafficTextLanguage,

    language: apiResult.language,
    factors: apiResult.factors ?? [],
    explanation: apiResult.explanation ?? '',

    // Preserve the complete backend response.
    raw: apiResult,
  }
}
