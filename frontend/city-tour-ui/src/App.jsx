import { useState } from 'react'
import PlanningForm from './components/PlanningForm'
import LayerToggles from './components/LayerToggles'
import MapView from './components/MapView'
import WeatherChart from './components/WeatherChart'
import StopList from './components/StopList'
import TrafficPanel from './components/TrafficPanel'
import { mockTour } from './data/mockTour'

export default function App() {
  const [tour, setTour] = useState(null) // null = no tour requested yet
  const [loading, setLoading] = useState(false)
  const [layers, setLayers] = useState({ weather: true, traffic: true, places: true })
  const [trafficError, setTrafficError] = useState(false) // TEMPORARY: for testing the error state

  function handleGenerate(request) {
    setLoading(true)
    console.log('Tour request (will go to the Java backend later):', request)
    // TEMPORARY: simulates the backend call. Replace with fetch('/api/tour?...') later.
    setTimeout(() => {
      setTour(mockTour)
      setLoading(false)
    }, 800)
  }

  function toggleLayer(key) {
    setLayers((prev) => ({ ...prev, [key]: !prev[key] }))
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-300 bg-white px-12 py-5">
        <span className="text-2xl font-semibold text-teal-700">City Tour Planner</span>
      </header>

      <main className="mx-auto max-w-6xl p-8">
        {!tour ? (
          <div className="mx-auto max-w-xl">
            <PlanningForm onGenerate={handleGenerate} loading={loading} />
          </div>
        ) : (
          <div className="flex flex-wrap gap-6">
            <aside className="w-full space-y-4 md:w-64">
              <LayerToggles layers={layers} onToggle={toggleLayer} />
              <button
                type="button"
                onClick={() => setTour(null)}
                className="h-11 w-full rounded-lg border border-teal-700 font-bold"
              >
                Change settings
              </button>
              {/* TEMPORARY test button: remove before submitting */}
              <button
                type="button"
                onClick={() => setTrafficError(!trafficError)}
                className="h-11 w-full rounded-lg border border-slate-400 text-sm"
              >
                Test: toggle traffic error
              </button>
            </aside>

            <section className="min-w-0 flex-1 space-y-6">
              {/* The map is always shown. The layers only control what is drawn on it. */}
              
              <MapView
                stops={tour.stops}
                showPlaces={layers.places}
                traffic={layers.traffic && !trafficError ? tour.traffic : []}
              />

              <div className="flex flex-wrap gap-6">
                {layers.weather && (
                  <div className="min-w-72 flex-1 rounded-xl border border-slate-300 bg-white p-5">
                    <h2 className="mb-3 text-lg font-bold">Hourly weather</h2>
                    <WeatherChart data={tour.weather} />
                  </div>
                )}
                {layers.places && (
                  <div className="min-w-72 flex-1">
                    <StopList stops={tour.stops} />
                  </div>
                )}
              </div>

              {layers.traffic && (
                <TrafficPanel announcements={tour.traffic} error={trafficError} />
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  )
}