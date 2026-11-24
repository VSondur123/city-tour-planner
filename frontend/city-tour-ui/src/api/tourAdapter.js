/** Adapt the documented Spring Boot response for the React components. */
function localTime(value) {
  if (!value) return '--:--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--:--'
  return new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Europe/Helsinki', hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
  }).format(date)
}

function adaptStops(stops = []) {
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
    time: `${localTime(stop.start)}–${localTime(stop.end)}`,
  }))
}

function adaptForecast(forecast = []) {
  return forecast.map((hour) => ({
    time: hour.time,
    hour: localTime(hour.time),
    temp: hour.temperature,
    rain: hour.precipitationAmount,
    cloudCover: hour.cloudCover,
    wind: hour.wind,
  }))
}

/** A Digitraffic geometry path is either a point (1 coordinate) or a polyline (2+). */
function adaptTrafficGeometry(announcements = []) {
  return announcements.flatMap((announcement, announcementIndex) =>
    (announcement.geometry?.paths ?? []).flatMap((path, pathIndex) => {
      if (!Array.isArray(path)) return []
      const coordinates = path.filter((point) =>
        Number.isFinite(point?.lat) && Number.isFinite(point?.lon))
      if (!coordinates.length) return []
      const common = {
        id: `traffic-${announcementIndex}-${pathIndex}`,
        type: announcement.type,
        description: announcement.description,
        area: announcement.area,
        language: announcement.language,
        startTime: announcement.startTime,
        endTime: announcement.endTime,
      }
      if (coordinates.length === 1) {
        return [{ ...common, kind: 'point', point: [coordinates[0].lat, coordinates[0].lon] }]
      }
      return [{
        ...common,
        kind: 'line',
        line: coordinates.map(({ lon, lat }) => [lon, lat]),
      }]
    }))
}

export function adaptTourResult(response) {
  return {
    language: response.language,
    factors: response.factors ?? [],
    stops: adaptStops(response.stops ?? []),
    weather: adaptForecast(response.forecast ?? []),
    announcements: response.announcements ?? [],
    traffic: adaptTrafficGeometry(response.announcements ?? []),
    weatherStatus: response.weatherStatus,
    trafficStatus: response.trafficStatus,
    trafficTextLanguage: response.trafficTextLanguage,
    explanation: response.explanation ?? '',
    raw: response,
  }
}
