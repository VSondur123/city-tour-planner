import { useEffect, useState } from 'react'
import { getCities } from '../api/tourApi'
import { getTranslations } from '../i18n/translations'

function todayIso() { const parts = new Intl.DateTimeFormat('en-GB', { timeZone: 'Europe/Helsinki', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(new Date()); const p = (k) => parts.find(x => x.type === k)?.value; return `${p('year')}-${p('month')}-${p('day')}` }
export default function PlanningForm({ onGenerate, loading, initialRequest = null, language = 'en', onCityChange, factors = ['WEATHER', 'TRAFFIC'], options = null }) {
  const t = getTranslations(language)
  const [cities, setCities] = useState([])
  const [citiesLoading, setCitiesLoading] = useState(true)
  const [citiesError, setCitiesError] = useState('')
  const [city, setCity] = useState(initialRequest?.city ?? 'tampere')
  const [date, setDate] = useState(initialRequest?.date ?? todayIso())
  const [time, setTime] = useState(initialRequest?.startTime ?? '13:00')
  const [duration, setDuration] = useState(initialRequest?.durationHours ?? 4)
  useEffect(() => { let active = true; getCities().then(result => { if (!active) return; const list = Array.isArray(result) ? result : []; setCities(list); setCitiesLoading(false); setCity(prev => list.some(x => x.id === prev) ? prev : (list[0]?.id ?? '')) }).catch(error => { if (active) { setCitiesError(error.message); setCitiesLoading(false) } }); return () => { active = false } }, [])
  useEffect(() => { onCityChange?.(city) }, [city, onCityChange])
  function submit(event) { event.preventDefault(); if (loading || citiesLoading || !city) return; onGenerate({ city, date, startTime: time, durationHours: duration, language, factors }) }
  return <form className="planning-form" onSubmit={submit}>
    <p className="planning-intro">{language === 'fi' ? 'Valitse kaupunki ja ajankohta. Luo oma kierroksesi sää- ja liikennetietojen avulla.' : 'Set your preferences and generate an optimized tour with places, weather and traffic.'}</p>
    <div className="planning-field"><label htmlFor="city-choice">⌖ &nbsp; {t.city}</label><select id="city-choice" value={city} disabled={citiesLoading || !!citiesError} onChange={e => setCity(e.target.value)}>{citiesLoading && <option value="">{t.loadingCities}</option>}{!citiesLoading && cities.length === 0 && <option value="">{t.noCities}</option>}{cities.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select>{citiesError && <p className="field-error" role="alert">{t.failedCities}: {citiesError}</p>}</div>
    <div className="planning-two"><div className="planning-field"><label htmlFor="tour-date">▦ &nbsp; {t.date}</label><input id="tour-date" type="date" required min={todayIso()} value={date} onChange={e => setDate(e.target.value)} /></div><div className="planning-field"><label htmlFor="tour-time">◷ &nbsp; {t.startTime}</label><input id="tour-time" type="time" required value={time} onChange={e => setTime(e.target.value)} /></div></div>
    <div className="planning-field"><label htmlFor="tour-duration">◴ &nbsp; {language === 'fi' ? 'Kierroksen kesto' : 'Tour duration'}</label><select id="tour-duration" value={duration} onChange={e => setDuration(Number(e.target.value))}>{[2, 4, 6].map(v => <option key={v} value={v}>{v} {language === 'fi' ? 'tuntia' : 'hours'}</option>)}</select></div>
    {options}
    <button type="submit" className="generate-button" disabled={loading || citiesLoading || !city}>✦ &nbsp; {loading ? t.buildingTour : t.generateTour}</button>
  </form>
}
