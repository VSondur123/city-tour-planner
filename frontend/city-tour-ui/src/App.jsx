
import { useState } from 'react'

import PlanningForm from './components/PlanningForm'
import LayerToggles from './components/LayerToggles'
import MapView from './components/MapView'
import WeatherChart from './components/WeatherChart'
import StopList from './components/StopList'
import TrafficPanel from './components/TrafficPanel'

import { generateTour } from './api/tourApi'
import { adaptTourResult } from './api/tourAdapter'

function getErrorMessage(error) {
  if (error.code === 'CITY_NOT_FOUND') {
    return 'The selected city is not supported.'
  }

  if (error.code === 'UNSUPPORTED_LANGUAGE') {
    return 'The selected language is not supported.'
  }

  if (error.code === 'VALIDATION_FAILED') {
    const pastDate = error.fields?.some(
      (field) => field.field === 'date' && field.code === 'PAST'
    )

    if (pastDate) {
      return 'Please select today or a future date.'
    }

    return 'Please check your tour settings.'
  }

  if (error.code === 'MALFORMED_REQUEST') {
    return 'The tour request has an invalid format.'
  }

  return error.message || 'Failed to generate the tour.'
}

export default function App() {
  const [tour, setTour] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [layers, setLayers] = useState({
    weather: true,
    traffic: true,
    places: true,
  })

  async function handleGenerate(request) {
    setLoading(true)
    setError('')

    try {
      console.log('Sending tour request:', request)

      const result = await generateTour(request)

      console.log('Backend tour response:', result)

      const adaptedTour = adaptTourResult(result)

      console.log('Adapted frontend tour:', adaptedTour)

      setTour(adaptedTour)
    } catch (err) {
      console.error('Failed to generate tour:', err)
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  function toggleLayer(key) {
    setLayers((previous) => ({
      ...previous,
      [key]: !previous[key],
    }))
  }

  function handleChangeSettings() {
    setTour(null)
    setError('')
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-300 bg-white px-6 py-5 md:px-12">
        <span className="text-2xl font-semibold text-teal-700">
          City Tour Planner
        </span>
      </header>

      <main className="mx-auto max-w-6xl p-4 md:p-8">
        {!tour ? (
          <div className="mx-auto max-w-xl space-y-4">
            <PlanningForm
              onGenerate={handleGenerate}
              loading={loading}
            />

            {error && (
              <div
                role="alert"
                className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-800"
              >
                <strong>Could not generate tour.</strong>
                <p className="mt-1">{error}</p>
              </div>
            )}
          </div>
        ) : (
          <div className="flex flex-wrap gap-6">
            <aside className="w-full space-y-4 md:w-64">
              <LayerToggles
                layers={layers}
                onToggle={toggleLayer}
              />

              <button
                type="button"
                onClick={handleChangeSettings}
                className="h-11 w-full rounded-lg border border-teal-700 font-bold text-teal-800"
              >
                Change settings
              </button>
            </aside>

            <section className="min-w-0 flex-1 space-y-6">
              <MapView
                stops={tour.stops}
                showPlaces={layers.places}
                traffic={
                  layers.traffic && tour.trafficStatus === 'AVAILABLE'
                    ? tour.traffic
                    : []
                }
              />

              <div className="rounded-xl border border-slate-300 bg-white p-5">
                <h2 className="mb-2 text-lg font-bold">
                  Why this tour?
                </h2>
                <p className="text-slate-700">
                  {tour.explanation || 'No explanation available.'}
                </p>
              </div>

              {tour.stops.length === 0 && (
                <div
                  role="status"
                  className="rounded-xl border border-amber-300 bg-amber-50 p-4"
                >
                  <strong>No suitable stops found.</strong>
                  <p className="mt-1 text-sm">
                    Try a longer duration or different planning settings.
                  </p>
                </div>
              )}

              {layers.weather && tour.weatherStatus === 'UNAVAILABLE' && (
                <div
                  role="alert"
                  className="rounded-xl border border-amber-300 bg-amber-50 p-4"
                >
                  Weather data is unavailable.
                  The tour was generated without weather information.
                </div>
              )}

              <div className="flex flex-wrap gap-6">
                {layers.weather && (
                  <div className="min-w-72 flex-1 rounded-xl border border-slate-300 bg-white p-5">
                    <h2 className="mb-3 text-lg font-bold">
                      Hourly weather
                    </h2>

                    {tour.weatherStatus === 'EMPTY' ? (
                      <p className="text-slate-600">
                        No forecast is available for this date.
                      </p>
                    ) : tour.weatherStatus === 'UNAVAILABLE' ? (
                      <p className="text-slate-600">
                        Weather information could not be loaded.
                      </p>
                    ) : (
                      <WeatherChart data={tour.weather} />
                    )}
                  </div>
                )}

                {layers.places && (
                  <div className="min-w-72 flex-1">
                    <StopList stops={tour.stops} />
                  </div>
                )}
              </div>

              {layers.traffic && (
                <TrafficPanel
                  announcements={tour.announcements}
                  error={tour.trafficStatus === 'UNAVAILABLE'}
                />
              )}

              {layers.traffic &&
                tour.trafficStatus === 'AVAILABLE' &&
                tour.trafficTextLanguage && (
                  <p className="text-sm text-slate-600">
                    Traffic announcements are provided in language:{' '}
                    {tour.trafficTextLanguage.toUpperCase()}.
                  </p>
                )}
            </section>
          </div>
        )}
      </main>
    </div>
  )
}
