
import { useEffect, useMemo } from 'react'
import {
  MapContainer,
  TileLayer,
  Marker,
  CircleMarker,
  Polyline,
  Popup,
  useMap,
} from 'react-leaflet'
import L from 'leaflet'
import { Minus, Plus } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import { getTranslations } from '../i18n/translations'

const centers = {
  tampere: [61.4981, 23.7608],
  helsinki: [60.1699, 24.9384],
  vaasa: [63.0951, 21.6165],
  oulu: [65.0121, 25.4651],
}

const numberIcon = (number) =>
  L.divIcon({
    html: `<div style="width:39px;height:39px;display:flex;align-items:center;justify-content:center;border:3px solid white;border-radius:50%;background:#0f766e;color:white;font-size:15px;font-weight:800;box-shadow:0 4px 12px #0f172a55">${number}</div>`,
    className: '',
    iconSize: [39, 39],
    iconAnchor: [19, 19],
  })

function SyncMapSize() {
  const map = useMap()

  useEffect(() => {
    const container = map.getContainer()
    const parent = container.parentElement
    let frame = 0

    const update = () => {
      cancelAnimationFrame(frame)

      frame = requestAnimationFrame(() => {
        if (!map.getContainer().isConnected) return

        map.invalidateSize({
          pan: false,
          debounceMoveend: true,
        })
      })
    }

    const observer =
      typeof ResizeObserver !== 'undefined'
        ? new ResizeObserver(update)
        : null

    observer?.observe(container)

    if (parent) {
      observer?.observe(parent)
    }

    window.addEventListener('resize', update)
    document.addEventListener('transitionend', update, true)

    const initial = window.setTimeout(update, 60)
    const afterLayout = window.setTimeout(update, 280)

    update()

    return () => {
      observer?.disconnect()
      window.removeEventListener('resize', update)
      document.removeEventListener('transitionend', update, true)
      window.clearTimeout(initial)
      window.clearTimeout(afterLayout)
      cancelAnimationFrame(frame)
    }
  }, [map])

  return null
}

function ChangeView({ city, positions, focusPlace }) {
  const map = useMap()

  useEffect(() => {
    map.invalidateSize()

    if (positions.length > 1) {
      map.fitBounds(L.latLngBounds(positions), {
        padding: [78, 78],
        maxZoom: 14,
      })
    } else if (positions.length === 1) {
      map.setView(positions[0], 14)
    } else {
      map.setView(centers[city] ?? centers.tampere, 13)
    }
  }, [map, positions, city])

  useEffect(() => {
    if (
      focusPlace?.lat != null &&
      (focusPlace.lon != null || focusPlace.lng != null)
    ) {
      map.flyTo(
        [focusPlace.lat, focusPlace.lon ?? focusPlace.lng],
        15,
        { duration: 0.7 }
      )
    }
  }, [map, focusPlace])

  return null
}

function MapButtons() {
  const map = useMap()

  return (
    <div className="ct-map-controls">
      <button type="button" aria-label="Zoom in" onClick={() => map.zoomIn()}><Plus size={18} /></button>
      <button type="button" aria-label="Zoom out" onClick={() => map.zoomOut()}><Minus size={18} /></button>
    </div>
  )
}

export default function MapView({
  stops = [],
  traffic = [],
  allPlaces = [],
  showAllPlaces = false,
  showPlaces = true,
  language = 'en',
  city = 'tampere',
  focusPlace = null,
  onPlaceSelect,
}) {
  const t = getTranslations(language)

  const ids = useMemo(
    () => new Set(stops.map((stop) => stop.placeId)),
    [stops]
  )

  const otherPlaces = useMemo(
    () => allPlaces.filter((place) => !ids.has(place.id)),
    [allPlaces, ids]
  )

  const positions = useMemo(() => {
    const chosen = stops.map((item) => [
      item.lat,
      item.lng,
    ])

    if (chosen.length) {
      return chosen
    }

    if (showAllPlaces && showPlaces) {
      return otherPlaces.map((item) => [
        item.lat,
        item.lon,
      ])
    }

    return []
  }, [stops, showPlaces, showAllPlaces, otherPlaces])

  return (
    <div className="relative h-full w-full overflow-hidden bg-sky-100">
      <MapContainer
        center={centers[city] ?? centers.tampere}
        zoom={13}
        zoomControl={false}
        scrollWheelZoom
        className="h-full w-full"
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution="&copy; OpenStreetMap contributors"
        />

        <SyncMapSize />

        <ChangeView
          positions={positions}
          city={city}
          focusPlace={focusPlace}
        />

        <MapButtons />

        {showAllPlaces &&
          showPlaces &&
          otherPlaces.map((place) => (
            <CircleMarker
              key={place.id}
              center={[place.lat, place.lon]}
              radius={7}
              pathOptions={{
                color: '#fff',
                weight: 2,
                fillColor: '#0ea5a1',
                fillOpacity: 0.94,
              }}
              eventHandlers={{
                click: () => onPlaceSelect?.(place),
              }}
            >
              <Popup>
                <strong>{place.name}</strong>
                <p>
                  {t[`category${place.category}`] ??
                    place.category}
                  {' · '}
                  {place.indoor ? t.indoor : t.outdoor}
                </p>
                <p>
                  {place.visitMinutes} {t.minutes}
                </p>
                {place.link && (
                  <a
                    href={place.link}
                    target="_blank"
                    rel="noreferrer"
                  >
                    {t.morePlace}
                  </a>
                )}
              </Popup>
            </CircleMarker>
          ))}

        {showPlaces && (
          <>
            {stops.length > 1 && (
              <Polyline
                positions={positions}
                pathOptions={{
                  color: '#0f766e',
                  weight: 4,
                  dashArray: '7 9',
                  opacity: 0.93,
                }}
              />
            )}

            {stops.map((stop) => (
              <Marker
                key={stop.placeId}
                position={[stop.lat, stop.lng]}
                icon={numberIcon(stop.id)}
                eventHandlers={{
                  click: () => onPlaceSelect?.(stop),
                }}
              >
                <Popup>
                  <strong>
                    {stop.id}. {stop.name}
                  </strong>
                  <p>{stop.time}</p>
                  <p>
                    {stop.visitMinutes} {t.minutes}
                  </p>
                  {stop.link && (
                    <a
                      href={stop.link}
                      target="_blank"
                      rel="noreferrer"
                    >
                      {t.morePlace}
                    </a>
                  )}
                </Popup>
              </Marker>
            ))}
          </>
        )}

        {traffic.map((item) =>
          item.kind === 'point' ? (
            <CircleMarker
              key={item.id}
              center={item.point}
              radius={9}
              pathOptions={{
                color: '#fff',
                weight: 2,
                fillColor: '#c66a0a',
                fillOpacity: 0.95,
              }}
            >
              <Popup>
                <strong>{item.type}</strong>
                <p>{item.description}</p>
              </Popup>
            </CircleMarker>
          ) : (
            <Polyline
              key={item.id}
              positions={item.line.map(
                ([lon, lat]) => [lat, lon]
              )}
              pathOptions={{
                color: '#b45309',
                weight: 6,
                opacity: 0.82,
              }}
            >
              <Popup>
                <strong>{item.type}</strong>
                <p>{item.description}</p>
              </Popup>
            </Polyline>
          )
        )}
      </MapContainer>

      <div className="ct-map-legend">
        <span><i className="ct-legend-dot" />{t.mapStopsLegend}</span>
        <span><i className="ct-legend-dash" />{t.mapOrderLegend}</span>
        {showAllPlaces && <span><i className="ct-legend-dot ct-legend-dot-light" />{t.mapAllPlacesLegend}</span>}
        {!!traffic.length && <span><i className="ct-legend-line" />{t.mapTrafficLegend}</span>}
      </div>
    </div>
  )
}
