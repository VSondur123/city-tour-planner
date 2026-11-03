import { MapContainer, TileLayer, Marker, Polyline, Popup } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Numbered marker drawn with HTML (avoids the broken default marker icon problem in Vite)
const numberIcon = (n) =>
  L.divIcon({
    html: `<div class="flex h-9 w-9 items-center justify-center rounded-full bg-teal-700 font-bold text-white">${n}</div>`,
    className: '',
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  })

export default function MapView({ stops }) {
  const positions = stops.map((s) => [s.lat, s.lng]) // Leaflet uses [lat, lng]
  return (
    <MapContainer
      center={positions[0]}
      zoom={14}
      className="h-[420px] w-full rounded-xl"
    >
      <TileLayer
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution="&copy; OpenStreetMap contributors"
      />
      <Polyline positions={positions} pathOptions={{ dashArray: '8 6' }} />
      {stops.map((s) => (
        <Marker key={s.id} position={[s.lat, s.lng]} icon={numberIcon(s.id)}>
          <Popup>{s.name}</Popup>
        </Marker>
      ))}
    </MapContainer>
  )
}