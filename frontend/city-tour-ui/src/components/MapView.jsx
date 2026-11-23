
import {
  MapContainer,
  TileLayer,
  Marker,
  Polyline,
  Popup,
} from 'react-leaflet'

import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const numberIcon = (number) =>
  L.divIcon({
    html: `<div class="flex h-9 w-9 items-center justify-center rounded-full bg-teal-700 font-bold text-white">${number}</div>`,
    className: '',
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  })

export default function MapView({
  stops = [],
  traffic = [],
  showPlaces = true,
}) {
  const positions = stops.map((stop) => [
    stop.lat,
    stop.lng,
  ])

  // The backend may return an empty tour.
  // In that situation, do not initialize Leaflet
  // with an undefined center.
  if (positions.length === 0) {
    return (
      <div className="flex h-[420px] items-center justify-center rounded-xl border border-slate-300 bg-white p-6">
        <div className="text-center">
          <p className="text-lg font-bold text-slate-700">
            No stops to display
          </p>
          <p className="mt-2 text-sm text-slate-500">
            The backend did not find suitable places
            for this tour.
          </p>
        </div>
      </div>
    )
  }

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

      {showPlaces && (
        <>
          {positions.length > 1 && (
            <Polyline
              positions={positions}
              pathOptions={{
                color: '#0f766e',
                dashArray: '8 6',
              }}
            />
          )}

          {stops.map((stop) => (
            <Marker
              key={stop.placeId}
              position={[stop.lat, stop.lng]}
              icon={numberIcon(stop.id)}
            >
              <Popup>
                <strong>{stop.name}</strong>
                <p>{stop.time}</p>
              </Popup>
            </Marker>
          ))}
        </>
      )}

      {traffic.map((announcement) => (
        <Polyline
          key={announcement.id}
          positions={announcement.line.map(
            ([lng, lat]) => [lat, lng]
          )}
          pathOptions={{
            color: '#B45309',
            weight: 6,
          }}
        >
          <Popup>
            <strong>{announcement.type}</strong>
            <p>{announcement.description}</p>
          </Popup>
        </Polyline>
      ))}
    </MapContainer>
  )
}
