import { useEffect, useRef, useState } from 'react'
import PlanningForm from './components/PlanningForm'
import ResultControls from './components/ResultControls'
import LayerToggles from './components/LayerToggles'
import MapView from './components/MapView'
import WeatherChart from './components/WeatherChart'
import StopList from './components/StopList'
import TrafficPanel from './components/TrafficPanel'
import AllPlaces from './components/AllPlaces'
import { generateTour, getPlaces } from './api/tourApi'
import { adaptTourResult } from './api/tourAdapter'
import { getTranslations } from './i18n/translations'

function getErrorMessage(error, t) {
  if (error.code === 'CITY_NOT_FOUND') return t.unknownCity
  if (error.code === 'UNSUPPORTED_LANGUAGE') return t.unsupportedLanguage
  if (error.code === 'MALFORMED_REQUEST') return t.malformedRequest
  if (error.code === 'VALIDATION_FAILED') {
    const pastDate = error.fields?.some((issue) => issue.field === 'date' && issue.code === 'PAST')
    if (pastDate) return t.pastDate
    const issue = error.fields?.[0]
    if (issue) {
      const fieldName = t.apiFields[issue.field] ?? issue.field
      const issueText = t.apiValidation[issue.code] ?? t.invalidSettings
      return `${fieldName}: ${issueText}`
    }
    return t.invalidSettings
  }
  if (error.message?.startsWith('Cannot connect to the backend')) return t.connectionFailure
  return t.genericFailure
}

export default function App() {
  const [tour, setTour] = useState(null)
  const [tourRequest, setTourRequest] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [placesResult, setPlacesResult] = useState({ key: '', places: [], error: '' })
  const [showAllPlaces, setShowAllPlaces] = useState(false)
  const [layers, setLayers] = useState({ weather: true, traffic: true, places: true })
  const requestIdRef = useRef(0)
  const language = tourRequest?.language ?? 'en'
  const catalogueCity = tourRequest?.city
  const catalogueLanguage = tourRequest?.language
  const hasTour = tour !== null
  const catalogueKey = `${catalogueCity ?? ''}|${catalogueLanguage ?? ''}`
  const allPlaces = placesResult.key === catalogueKey ? placesResult.places : []
  const placesError = placesResult.key === catalogueKey ? placesResult.error : ''
  const placesLoading = hasTour && placesResult.key !== catalogueKey
  const t = getTranslations(language)

  async function handleGenerate(request) {
    const id = ++requestIdRef.current
    setLoading(true)
    setError('')
    try {
      const apiResult = await generateTour(request)
      if (id !== requestIdRef.current) return
      setTour(adaptTourResult(apiResult))
      setTourRequest(request)
    } catch (err) {
      if (id !== requestIdRef.current) return
      console.error('Tour API error:', err)
      setError(getErrorMessage(err, getTranslations(request.language)))
    } finally {
      if (id === requestIdRef.current) setLoading(false)
    }
  }

  // Catalogue fetching is independent of display layers; toggling them makes no API call.
  useEffect(() => {
    if (!hasTour || !catalogueCity || !catalogueLanguage) return undefined
    let active = true
    getPlaces(catalogueCity, catalogueLanguage)
      .then((result) => {
        if (active) setPlacesResult({
          key: catalogueKey, places: Array.isArray(result) ? result : [], error: '',
        })
      })
      .catch((err) => {
        if (active) setPlacesResult({
          key: catalogueKey, places: [],
          error: getErrorMessage(err, getTranslations(catalogueLanguage)),
        })
      })
    return () => { active = false }
  }, [catalogueCity, catalogueLanguage, catalogueKey, hasTour])

  function handleChangeSettings() {
    requestIdRef.current += 1
    setLoading(false)
    setError('')
    setTour(null)
    setShowAllPlaces(false)
  }

  return <div className="min-h-screen bg-slate-100" lang={language}>
    <header className="border-b border-slate-300 bg-white px-6 py-5 md:px-12"><span className="text-2xl font-semibold text-teal-700">{t.appTitle}</span></header>
    <main className="mx-auto max-w-6xl p-4 md:p-8">
      {!tour ? (
        <div className="mx-auto max-w-xl space-y-4">
          <PlanningForm onGenerate={handleGenerate} loading={loading} initialRequest={tourRequest} language={language} />
          {error && <div role="alert" className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-800"><strong>{t.generateFailure}</strong><p className="mt-1">{error}</p></div>}
        </div>
      ) : (
        <div className="flex flex-wrap gap-6">
          <aside className="w-full space-y-4 md:w-64">
            <ResultControls request={tourRequest} loading={loading} onChange={handleGenerate} />
            <LayerToggles layers={layers} onToggle={(key) => setLayers((previous) => ({ ...previous, [key]: !previous[key] }))} language={language} />
            <button type="button" onClick={handleChangeSettings} className="h-11 w-full rounded-lg border border-teal-700 font-bold text-teal-800">{t.changeSettings}</button>
          </aside>
          <section className="min-w-0 flex-1 space-y-6" aria-busy={loading}>
            {loading && <div role="status" className="rounded-xl border border-teal-200 bg-teal-50 p-4 text-sm font-medium text-teal-800">{t.updatingTour}</div>}
            {error && <div role="alert" className="rounded-xl border border-red-300 bg-red-50 p-4 text-red-800"><strong>{t.updateFailure}</strong><p className="mt-1">{error}</p><p className="mt-1 text-sm">{t.previousTour}</p></div>}
            <MapView stops={tour.stops} showPlaces={layers.places} allPlaces={allPlaces}
              showAllPlaces={showAllPlaces && layers.places}
              traffic={layers.traffic && tour.trafficStatus === 'AVAILABLE' ? tour.traffic : []} language={language} />
            <div className="rounded-xl border border-slate-300 bg-white p-5"><h2 className="mb-2 text-lg font-bold">{t.whyTour}</h2><p className="text-slate-700">{tour.explanation || t.noExplanation}</p></div>
            {tour.stops.length === 0 && <div role="status" className="rounded-xl border border-amber-300 bg-amber-50 p-4"><strong>{t.noStops}</strong><p className="mt-1 text-sm">{t.noStopsHint}</p></div>}
            {layers.weather && tour.weatherStatus === 'UNAVAILABLE' && <div role="alert" className="rounded-xl border border-amber-300 bg-amber-50 p-4">{t.weatherUnavailable}</div>}
            <div className="flex flex-wrap gap-6">
              {layers.weather && <div className="min-w-72 flex-1 rounded-xl border border-slate-300 bg-white p-5"><h2 className="mb-3 text-lg font-bold">{t.hourlyWeather}</h2>
                {tour.weatherStatus === 'EMPTY' ? <p className="text-slate-600">{t.forecastEmpty}</p> : tour.weatherStatus === 'UNAVAILABLE' ? <p className="text-slate-600">{t.forecastUnavailable}</p> : <WeatherChart data={tour.weather} language={language} />}</div>}
              {layers.places && <div className="min-w-72 flex-1"><StopList stops={tour.stops} language={language} /></div>}
            </div>
            {layers.places && <AllPlaces places={allPlaces} tourStops={tour.stops} loading={placesLoading}
              error={placesError} showOnMap={showAllPlaces} onToggleMap={setShowAllPlaces} language={language} />}
            {layers.traffic && <TrafficPanel announcements={tour.announcements} error={tour.trafficStatus === 'UNAVAILABLE'} language={language} />}
            {layers.traffic && tour.trafficStatus === 'AVAILABLE' && tour.trafficTextLanguage && <p className="text-sm text-slate-600">{t.trafficLanguage} {tour.trafficTextLanguage.toUpperCase()}.</p>}
          </section>
        </div>
      )}
    </main>
  </div>
}
