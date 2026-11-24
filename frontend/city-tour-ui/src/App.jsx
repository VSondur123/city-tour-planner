import { useCallback, useEffect, useRef, useState } from 'react'
import PlanningForm from './components/PlanningForm'
import LayerToggles from './components/LayerToggles'
import MapView from './components/MapView'
import WeatherChart from './components/WeatherChart'
import TrafficPanel from './components/TrafficPanel'
import AllPlaces from './components/AllPlaces'
import PlacePhoto from './components/PlacePhoto'
import { generateTour, getPlaces } from './api/tourApi'
import { adaptTourResult } from './api/tourAdapter'
import { getTranslations } from './i18n/translations'
import './dashboard.css'

const cityNames = { tampere: 'Tampere', helsinki: 'Helsinki', vaasa: 'Vaasa', oulu: 'Oulu' }
function apiError(error, t) {
  if (error.code === 'CITY_NOT_FOUND') return t.unknownCity
  if (error.code === 'UNSUPPORTED_LANGUAGE') return t.unsupportedLanguage
  if (error.code === 'MALFORMED_REQUEST') return t.malformedRequest
  if (error.code === 'VALIDATION_FAILED') {
    if (error.fields?.some(x => x.field === 'date' && x.code === 'PAST')) return t.pastDate
    const first = error.fields?.[0]
    return first ? `${t.apiFields[first.field] ?? first.field}: ${t.apiValidation[first.code] ?? t.invalidSettings}` : t.invalidSettings
  }
  if (error.message?.startsWith('Cannot connect to the backend')) return t.connectionFailure
  return t.genericFailure
}
function displayDate(value, language) {
  if (!value) return ''
  const date = new Date(`${value}T12:00:00+02:00`)
  return new Intl.DateTimeFormat(language === 'fi' ? 'fi-FI' : 'en-GB', { day: 'numeric', month: 'short', year: 'numeric', timeZone: 'Europe/Helsinki' }).format(date)
}
function compactTime(time) { return time?.split('–') ?? ['', ''] }

export default function App() {
  const [tour, setTour] = useState(null)
  const [tourRequest, setTourRequest] = useState(null)
  const [preferredLanguage, setPreferredLanguage] = useState('en')
  const [preferredFactors, setPreferredFactors] = useState(['WEATHER', 'TRAFFIC'])
  const [previewCity, setPreviewCity] = useState('tampere')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [catalogue, setCatalogue] = useState({ key: '', places: [], error: '' })
  const [showAllPlaces, setShowAllPlaces] = useState(true)
  const [placesOpen, setPlacesOpen] = useState(true)
  const [leftOpen, setLeftOpen] = useState(true)
  const [rightOpen, setRightOpen] = useState(true)
  const [mobilePlanning, setMobilePlanning] = useState(false)
  const [selectedPlace, setSelectedPlace] = useState(null)
  const [activeTab, setActiveTab] = useState('tour')
  const [layersOpen, setLayersOpen] = useState(false)
  const [weatherOpen, setWeatherOpen] = useState(false)
  const [trafficOpen, setTrafficOpen] = useState(false)
  const [layers, setLayers] = useState({ weather: true, traffic: true, places: true })
  const requestId = useRef(0)
  const language = tourRequest?.language ?? preferredLanguage
  const t = getTranslations(language)
  const city = tourRequest?.city ?? previewCity
  const cityName = cityNames[city] ?? city
  const catalogueKey = `${city}|${language}`
  const places = catalogue.key === catalogueKey ? catalogue.places : []
  const placesError = catalogue.key === catalogueKey ? catalogue.error : ''
  const placesLoading = catalogue.key !== catalogueKey
  const onCityChange = useCallback(value => setPreviewCity(value), [])
  const activeFactors = tourRequest?.factors ?? preferredFactors
  function changeLanguage(nextLanguage) {
    if (nextLanguage === language || loading) return
    if (tourRequest && tour) handleGenerate({ ...tourRequest, language: nextLanguage })
    else setPreferredLanguage(nextLanguage)
  }
  function toggleFactor(factor) {
    if (loading) return
    const nextFactors = activeFactors.includes(factor) ? activeFactors.filter(x => x !== factor) : [...activeFactors, factor]
    if (tourRequest && tour) handleGenerate({ ...tourRequest, factors: nextFactors })
    else setPreferredFactors(nextFactors)
  }

  useEffect(() => {
    let active = true
    getPlaces(city, language).then(data => { if (active) setCatalogue({ key: catalogueKey, places: Array.isArray(data) ? data : [], error: '' }) })
      .catch(err => { if (active) setCatalogue({ key: catalogueKey, places: [], error: apiError(err, getTranslations(language)) }) })
    return () => { active = false }
  }, [city, language, catalogueKey])

  async function handleGenerate(request) {
    const id = ++requestId.current
    setLoading(true); setError('')
    try {
      const response = await generateTour(request)
      if (id !== requestId.current) return
      setTour(adaptTourResult(response))
      setTourRequest(request)
      setPreferredLanguage(request.language)
      setPreferredFactors(request.factors)
      setMobilePlanning(false)
      setActiveTab('tour')
    } catch (err) {
      if (id !== requestId.current) return
      console.error('Tour request:', err)
      setError(apiError(err, getTranslations(request.language)))
    } finally { if (id === requestId.current) setLoading(false) }
  }
  function resetTour() { requestId.current += 1; setLoading(false); setPreferredLanguage(tourRequest?.language ?? preferredLanguage); setPreferredFactors(tourRequest?.factors ?? preferredFactors); setTourRequest(null); setTour(null); setError(''); setMobilePlanning(true) }
  function explorePlace(place) { setSelectedPlace(place); setShowAllPlaces(true) }
  const forecast = tour?.weather ?? []
  const previewHours = forecast.filter(x => x.temp != null).slice(0, 4)
  const firstThree = tour?.announcements?.slice(0, 3) ?? []

  return <div className="ct-dashboard" lang={language}>
    <header className="ct-header">
      <a href="#tour-map" className="ct-brand"><span className="ct-brand-icon">⌖</span><span>City Tour Planner</span></a>
      <nav className="ct-navigation" aria-label="Main navigation">
        <button type="button" className="ct-nav-selected" onClick={() => { setLeftOpen(true); setMobilePlanning(true) }}>▱ <span>{language === 'fi' ? 'Suunnittele' : 'Plan Tour'}</span></button>
        <button type="button" onClick={() => { setPlacesOpen(true); document.getElementById('places-panel')?.scrollIntoView({ behavior: 'smooth', block: 'nearest' }) }}>⌖ <span>{language === 'fi' ? 'Kohteet' : 'All Places'}</span></button>
      </nav>
      <div className="ct-header-right"><span className="ct-city-name">{cityName}</span><span className="ct-language-pill">{language.toUpperCase()}</span></div>
    </header>
    <main className="ct-workspace">
      <section id="tour-map" className="ct-map-area" aria-label="City tour map">
        <MapView city={city} stops={tour?.stops ?? []} traffic={layers.traffic && tour?.trafficStatus === 'AVAILABLE' ? tour.traffic : []} allPlaces={places} showAllPlaces={showAllPlaces && layers.places} showPlaces={layers.places} language={language} onPlaceSelect={explorePlace} focusPlace={selectedPlace} />
      </section>

      <aside className={`ct-panel ct-left-panel ${!leftOpen ? 'ct-closed' : ''} ${mobilePlanning ? 'ct-mobile-visible' : ''}`} aria-label="Tour planning">
        <div className="ct-panel-top"><div className="ct-panel-heading"><span className="ct-accent-icon">✧</span><h2>{language === 'fi' ? 'Suunnittele kierros' : 'Plan your city tour'}</h2></div><button type="button" className="ct-panel-collapse" aria-label={leftOpen ? 'Collapse planning panel' : 'Expand planning panel'} onClick={() => setLeftOpen(v => !v)}>{leftOpen ? '‹' : '›'}</button></div>
        {leftOpen && <div className="ct-left-content">
          <PlanningForm onGenerate={handleGenerate} loading={loading} initialRequest={tourRequest} language={language} factors={activeFactors} onCityChange={onCityChange} options={<section className="ct-form-options">
            <div className="ct-language-setting"><label>{t.language}</label><div className="ct-language-segments"><button type="button" aria-pressed={language === 'en'} className={language === 'en' ? 'chosen' : ''} disabled={loading} onClick={() => changeLanguage('en')}>EN</button><button type="button" aria-pressed={language === 'fi'} className={language === 'fi' ? 'chosen' : ''} disabled={loading} onClick={() => changeLanguage('fi')}>FI</button></div></div>
            <div className="ct-factor-settings"><h3>▱ &nbsp; {t.planningFactors}</h3><label><span>☁ &nbsp; {t.useWeather}</span><input type="checkbox" role="switch" checked={activeFactors.includes('WEATHER')} disabled={loading} onChange={() => toggleFactor('WEATHER')} /></label><label><span>▤ &nbsp; {t.useTraffic}</span><input type="checkbox" role="switch" checked={activeFactors.includes('TRAFFIC')} disabled={loading} onChange={() => toggleFactor('TRAFFIC')} /></label><p>{t.factorsHint}</p></div>
          </section>} />
          <div className="ct-options-area"><button type="button" className="ct-section-trigger" aria-expanded={layersOpen} onClick={() => setLayersOpen(v => !v)}>▱ &nbsp; {t.dataLayers}<span>{layersOpen ? '−' : '+'}</span></button>{layersOpen && <div className="ct-options-inner"><LayerToggles layers={layers} language={language} onToggle={name => setLayers(p => ({ ...p, [name]: !p[name] }))} /></div>}</div>
          {tour && <button type="button" className="ct-secondary-button" onClick={resetTour}>{t.changeSettings}</button>}
          {error && <p className="ct-error" role="alert">{error}</p>}
          {loading && <p className="ct-loading" role="status">{t.buildingTour}</p>}
          <button type="button" className="ct-mobile-close" onClick={() => setMobilePlanning(false)}>{language === 'fi' ? 'Sulje' : 'Close'}</button>
        </div>}
      </aside>

      {selectedPlace && <section className="ct-selected-place" aria-label="Selected place"><button className="ct-selected-close" type="button" onClick={() => setSelectedPlace(null)} aria-label="Close">×</button><strong>{selectedPlace.name}</strong><p>{t[`category${selectedPlace.category}`] ?? selectedPlace.category} · {selectedPlace.indoor ? t.indoor : t.outdoor} · {selectedPlace.visitMinutes} min</p>{selectedPlace.link && <a href={selectedPlace.link} target="_blank" rel="noreferrer">{t.morePlace} ↗</a>}</section>}

      <aside className={`ct-panel ct-right-panel ${!rightOpen ? 'ct-closed' : ''}`} aria-label="Tour details">
        <div className="ct-right-tabs"><button type="button" className={activeTab === 'tour' ? 'active' : ''} onClick={() => { setActiveTab('tour'); setRightOpen(true) }}>▱ &nbsp; {language === 'fi' ? 'Kierroksen tiedot' : 'Tour details'}</button><button type="button" className={activeTab === 'why' ? 'active' : ''} onClick={() => { setActiveTab('why'); setRightOpen(true) }}>✧ &nbsp; {t.whyTour}</button><button type="button" className="ct-right-collapse" onClick={() => setRightOpen(v => !v)} aria-label="Toggle details">{rightOpen ? '›' : '‹'}</button></div>
        {rightOpen && <div className="ct-right-content">
          {tour ? <>
            <h2 className="ct-tour-name">{cityName} City Tour</h2>
            <div className="ct-tour-meta"><span>▦ &nbsp; {displayDate(tourRequest.date, language)}</span><span>◷ &nbsp; {tourRequest.startTime} · {tourRequest.durationHours} {t.hours}</span></div>
            {activeTab === 'why' && <div className="ct-why-box"><strong>✧ &nbsp; {t.whyTour}</strong><p>{tour.explanation || t.noExplanation}</p></div>}
            <section className="ct-stops-summary"><h3>{t.tourStops} ({tour.stops.length})</h3>{tour.stops.length === 0 ? <p className="ct-empty">{t.noStopsHint}</p> : <div className="ct-stop-list">{tour.stops.map(stop => <button type="button" key={stop.placeId} onClick={() => explorePlace(stop)} className="ct-stop-item"><span className="ct-stop-number">{stop.id}</span><span className="ct-stop-copy"><strong title={stop.name}>{stop.name}</strong><small>{compactTime(stop.time).join(' – ')} ({stop.visitMinutes} min)</small></span><PlacePhoto category={stop.category} name={stop.name} className="ct-stop-thumb"/><span className="ct-stop-chevron">›</span></button>)}</div>}</section>
            {layers.weather && <section className="ct-weather-summary"><button type="button" className="ct-summary-heading" onClick={() => setWeatherOpen(v => !v)} aria-expanded={weatherOpen}><span className="ct-accent-icon">✧</span><strong>{t.hourlyWeather}</strong><span>›</span></button><p className="ct-summary-subtitle">{cityName}, {tourRequest.startTime}</p>{tour.weatherStatus !== 'AVAILABLE' ? <p className="ct-empty">{tour.weatherStatus === 'EMPTY' ? t.forecastEmpty : t.forecastUnavailable}</p> : <><div className="ct-weather-hours">{previewHours.length ? previewHours.map((point, idx) => <div key={`${point.time}-${idx}`} className="ct-weather-hour"><span>{point.hour}</span><span className="ct-weather-symbol" aria-hidden="true">☁</span><strong>{point.temp == null ? '—' : `${point.temp.toFixed(1)}°C`}</strong></div>) : <p className="ct-empty">{t.noHourlyWeather}</p>}</div>{weatherOpen && <div className="ct-expanded-chart"><WeatherChart data={forecast} language={language}/></div>}</>}</section>}
            {layers.traffic && <section className="ct-traffic-summary"><button type="button" className="ct-summary-heading" onClick={() => setTrafficOpen(v => !v)} aria-expanded={trafficOpen}><span className="ct-accent-icon">▤</span><strong>{t.trafficTitle} ({tour.announcements.length})</strong><span>›</span></button>{tour.trafficStatus === 'UNAVAILABLE' ? <p className="ct-empty">{t.trafficUnavailableTitle}</p> : !firstThree.length ? <p className="ct-empty">{t.trafficEmpty}</p> : <div className="ct-traffic-list">{firstThree.map((a, i) => <div className="ct-traffic-notice" key={`${a.startTime}-${i}`}><span>⚠</span><div><strong title={a.type}>{a.type}</strong><small>{a.area}</small></div><span>›</span></div>)}</div>}{trafficOpen && <div className="ct-expanded-traffic"><TrafficPanel announcements={tour.announcements} error={tour.trafficStatus === 'UNAVAILABLE'} language={language}/></div>}{tour.trafficTextLanguage && <p className="ct-source-language">{t.trafficLanguage} {tour.trafficTextLanguage.toUpperCase()}</p>}</section>}
            {error && <p className="ct-error" role="alert">{error}</p>}{loading && <p className="ct-loading" role="status">{t.updatingTour}</p>}
          </> : <div className="ct-empty-tour"><span>⌖</span><h2>{language === 'fi' ? 'Kierroksesi näkyy täällä' : 'Your tour appears here'}</h2><p>{language === 'fi' ? 'Luo kierros vasemman paneelin avulla.' : 'Create a tour using the planning panel to see stops, weather and traffic here.'}</p></div>}
        </div>}
      </aside>

      <section id="places-panel" className={`ct-places-panel ${!placesOpen ? 'ct-places-closed' : ''}`} aria-label="All places"><button type="button" className="ct-places-handle" onClick={() => setPlacesOpen(v => !v)} aria-label={placesOpen ? 'Collapse places' : 'Expand places'}>{placesOpen ? '⌄' : '⌃'}</button>{placesOpen ? <AllPlaces places={places} tourStops={tour?.stops ?? []} loading={placesLoading} error={placesError} showOnMap={showAllPlaces} onToggleMap={setShowAllPlaces} language={language} cityName={cityName} onPlaceSelect={explorePlace}/> : <button className="ct-collapsed-title" type="button" onClick={() => setPlacesOpen(true)}>⌖ &nbsp; {t.allPlacesTitle} ({places.length})</button>}</section>
      <button className="ct-mobile-plan-button" type="button" onClick={() => { setLeftOpen(true); setMobilePlanning(v => !v) }}>{mobilePlanning ? '×' : '✧'} &nbsp; {language === 'fi' ? 'Suunnittele kierros' : 'Plan your tour'}</button>
      {mobilePlanning && <button className="ct-mobile-backdrop" type="button" aria-label="Close planning panel" onClick={() => setMobilePlanning(false)} />}
    </main>
  </div>
}
