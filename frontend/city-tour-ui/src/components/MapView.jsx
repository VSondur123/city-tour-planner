import { useEffect, useMemo } from 'react'
import {
  MapContainer, TileLayer, Marker, CircleMarker, Polyline, Popup, useMap,
} from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { getTranslations } from '../i18n/translations'

const numberIcon = (number) => L.divIcon({
  html: `<div class="flex h-9 w-9 items-center justify-center rounded-full bg-teal-700 font-bold text-white">${number}</div>`,
  className: '', iconSize: [36, 36], iconAnchor: [18, 18],
})

function FitBounds({ positions }) {
  const map = useMap()
  useEffect(() => {
    if (positions.length === 1) map.setView(positions[0], 14)
    else if (positions.length > 1) map.fitBounds(L.latLngBounds(positions), {
      padding: [40, 40], maxZoom: 15,
    })
  }, [map, positions])
  return null
}

export default function MapView({
  stops = [], traffic = [], allPlaces = [], showAllPlaces = false,
  showPlaces = true, language = 'en',
}) {
  const t = getTranslations(language)
  const selectedIds = useMemo(() => new Set(stops.map((stop) => stop.placeId)), [stops])
  const otherPlaces = useMemo(() => allPlaces.filter((place) => !selectedIds.has(place.id)), [allPlaces, selectedIds])
  const boundsPositions = useMemo(() => {
    const stopPositions = showPlaces ? stops.map((stop) => [stop.lat, stop.lng]) : []
    const extraPositions = showPlaces && showAllPlaces ? otherPlaces.map((place) => [place.lat, place.lon]) : []
    const trafficPositions = traffic.flatMap((item) => item.kind === 'point' ? [item.point] :
      item.line?.map(([lon, lat]) => [lat, lon]) ?? [])
    return [...stopPositions, ...extraPositions, ...(!stopPositions.length && !extraPositions.length ? trafficPositions : [])]
  }, [stops, showPlaces, showAllPlaces, otherPlaces, traffic])

  if (!boundsPositions.length) return (
    <div className="flex h-[420px] items-center justify-center rounded-xl border border-slate-300 bg-white p-6">
      <div className="text-center"><p className="text-lg font-bold text-slate-700">{t.mapNoStops}</p>
        <p className="mt-2 text-sm text-slate-500">{t.mapNoStopsHint}</p></div>
    </div>
  )

  const showOther = showPlaces && showAllPlaces
  return <section className="overflow-hidden rounded-xl border border-slate-300 bg-white">
    <MapContainer center={boundsPositions[0]} zoom={14} scrollWheelZoom className="h-[420px] w-full">
      <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors" />
      <FitBounds positions={boundsPositions} />
      {showOther && otherPlaces.map((place) => <CircleMarker key={`place-${place.id}`}
        center={[place.lat, place.lon]} radius={6}
        pathOptions={{ color: '#0369a1', fillColor: '#38bdf8', fillOpacity: 0.9, weight: 2 }}>
        <Popup>
          <strong>{place.name}</strong>
          <p>{t[`category${place.category}`] ?? place.category} · {place.indoor ? t.indoor : t.outdoor}</p>
          <p>{t.mapVisit}: {place.visitMinutes} {t.minutes}</p>
          {place.link && <a href={place.link} target="_blank" rel="noopener noreferrer">{t.mapPlaceLink}</a>}
        </Popup>
      </CircleMarker>)}
      {showPlaces && <>
        {stops.length > 1 && <Polyline positions={stops.map((stop) => [stop.lat, stop.lng])}
          pathOptions={{ color: '#0f766e', weight: 3, dashArray: '8 6' }} />}
        {stops.map((stop) => <Marker key={stop.placeId} position={[stop.lat, stop.lng]} icon={numberIcon(stop.id)}>
          <Popup><strong>{stop.id}. {stop.name}</strong><p>{stop.time}</p>
            <p>{t.mapVisit}: {stop.visitMinutes} {t.minutes}</p>
            {stop.link && <a href={stop.link} target="_blank" rel="noopener noreferrer">{t.mapPlaceLink}</a>}
          </Popup>
        </Marker>)}
      </>}
      {traffic.map((announcement) => announcement.kind === 'point' ?
        <CircleMarker key={announcement.id} center={announcement.point} radius={9}
          pathOptions={{ color: '#92400e', fillColor: '#f59e0b', fillOpacity: 0.9, weight: 3 }}>
          <Popup><strong>{announcement.type}</strong><p>{announcement.description}</p><p>{announcement.area}</p></Popup>
        </CircleMarker> : <Polyline key={announcement.id}
          positions={announcement.line.map(([lng, lat]) => [lat, lng])}
          pathOptions={{ color: '#b45309', weight: 6, opacity: 0.8 }}>
          <Popup><strong>{announcement.type}</strong><p>{announcement.description}</p><p>{announcement.area}</p></Popup>
        </Polyline>)}
    </MapContainer>
    <div className="flex flex-wrap gap-5 border-t border-slate-200 px-4 py-3 text-sm">
      {showPlaces && <>
        {stops.length > 0 && <div className="flex items-center gap-2"><span className="h-3 w-3 rounded-full bg-teal-700" />{t.mapStopsLegend}</div>}
        {stops.length > 1 && <div className="flex items-center gap-2"><span className="w-7 border-t-2 border-dashed border-teal-700" />{t.mapOrderLegend}</div>}
        {showOther && otherPlaces.length > 0 && <div className="flex items-center gap-2"><span className="h-3 w-3 rounded-full bg-sky-400" />{t.mapAllPlacesLegend}</div>}
      </>}
      {traffic.some((item) => item.kind === 'line') && <div className="flex items-center gap-2"><span className="h-1 w-7 bg-amber-700" />{t.mapTrafficLegend}</div>}
      {traffic.some((item) => item.kind === 'point') && <div className="flex items-center gap-2"><span className="h-3 w-3 rounded-full bg-amber-500" />{t.mapTrafficPointLegend}</div>}
    </div>
  </section>
}
