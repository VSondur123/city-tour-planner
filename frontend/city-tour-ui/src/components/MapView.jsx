import { useEffect, useMemo } from 'react'
import { MapContainer, TileLayer, Marker, Polyline, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { getTranslations } from '../i18n/translations'

const numberIcon = (number) => L.divIcon({
  html: `<div class="flex h-9 w-9 items-center justify-center rounded-full bg-teal-700 font-bold text-white">${number}</div>`,
  className: '', iconSize: [36, 36], iconAnchor: [18, 18],
})

function FitMapToStops({ positions }) {
  const map = useMap()
  useEffect(() => {
    if (positions.length === 0) return
    if (positions.length === 1) map.setView(positions[0], 14)
    else map.fitBounds(L.latLngBounds(positions), { padding: [40, 40], maxZoom: 15 })
  }, [map, positions])
  return null
}

export default function MapView({ stops = [], traffic = [], showPlaces = true, language = 'en' }) {
  const t = getTranslations(language)
  const positions = useMemo(() => stops.map((stop) => [stop.lat, stop.lng]), [stops])
  if (positions.length === 0) return <div className="flex h-[420px] items-center justify-center rounded-xl border border-slate-300 bg-white p-6"><div className="text-center"><p className="text-lg font-bold text-slate-700">{t.mapNoStops}</p><p className="mt-2 text-sm text-slate-500">{t.mapNoStopsHint}</p></div></div>
  return <section className="overflow-hidden rounded-xl border border-slate-300 bg-white">
    <MapContainer center={positions[0]} zoom={14} scrollWheelZoom className="h-[420px] w-full">
      <TileLayer url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap contributors" />
      <FitMapToStops positions={positions} />
      {showPlaces && <>
        {positions.length > 1 && <Polyline positions={positions} pathOptions={{ color: '#0f766e', weight: 3, dashArray: '8 6' }} />}
        {stops.map((stop) => <Marker key={stop.placeId} position={[stop.lat, stop.lng]} icon={numberIcon(stop.id)}>
          <Popup><div><strong>{stop.id}. {stop.name}</strong><p>{stop.time}</p><p>{t.mapVisit}: {stop.visitMinutes} {t.minutes}</p>{stop.link && <a href={stop.link} target="_blank" rel="noopener noreferrer">{t.mapPlaceLink}</a>}</div></Popup>
        </Marker>)}
      </>}
      {traffic.map((announcement) => <Polyline key={announcement.id} positions={announcement.line.map(([lng, lat]) => [lat, lng])} pathOptions={{ color: '#B45309', weight: 6, opacity: 0.8 }}>
        <Popup><strong>{announcement.type}</strong><p>{announcement.description}</p><p>{announcement.area}</p></Popup>
      </Polyline>)}
    </MapContainer>
    <div className="flex flex-wrap gap-5 border-t border-slate-200 px-4 py-3 text-sm">
      {showPlaces && <><div className="flex items-center gap-2"><span className="h-3 w-3 rounded-full bg-teal-700" /><span>{t.mapStopsLegend}</span></div><div className="flex items-center gap-2"><span className="w-7 border-t-2 border-dashed border-teal-700" /><span>{t.mapOrderLegend}</span></div></>}
      {traffic.length > 0 && <div className="flex items-center gap-2"><span className="h-1 w-7 bg-amber-700" /><span>{t.mapTrafficLegend}</span></div>}
    </div>
  </section>
}
