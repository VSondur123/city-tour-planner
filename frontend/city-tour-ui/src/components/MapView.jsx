import { MapContainer, TileLayer, Marker, Polyline, Popup } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Numbered marker drawn with HTML (avoids the broken default marker icon in Vite)
const numberIcon = (n) =>
  L.divIcon({
    html: `<div class="flex h-9 w-9 items-center justify-center rounded-full bg-teal-700 font-bold text-white">${n}</div>`,
    className: '',
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  })

export default function MapView({ stops, traffic = [], showPlaces = true }) {
  const positions = stops.map((s) => [s.lat, s.lng]) // Leaflet uses [lat, lng]

  return (
    <MapContainer center={positions[0]} zoom={14} className="h-[420px] w-full rounded-xl">
      <TileLayer
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&copy; OpenStreetMap contributors"
      />

      {/* Places layer: only the stops and the visit order. The base map always stays. */}
      {showPlaces && (
        <>
          {/* Suggested visit order, not a walking route */}
          <Polyline positions={positions} pathOptions={{ color: '#0f766e', dashArray: '8 6' }} />
          {stops.map((s) => (
            <Marker key={s.id} position={[s.lat, s.lng]} icon={numberIcon(s.id)}>
              <Popup>{s.name}</Popup>
            </Marker>
          ))}
        </>
      )}

      {traffic.map((a) => (
        <Polyline
          key={a.id}
          // Swap order: GeoJSON is [lng, lat], Leaflet needs [lat, lng]
          positions={a.line.map(([lng, lat]) => [lat, lng])}
          pathOptions={{ color: '#B45309', weight: 6 }}
        >
          <Popup>
            {a.type}: {a.description}
          </Popup>
        </Polyline>
      ))}
    </MapContainer>
  )
}