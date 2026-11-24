import { useMemo, useState } from 'react'
import { Check, Clock, ExternalLink, MapPin, Search, SlidersHorizontal } from 'lucide-react'
import { getTranslations } from '../i18n/translations'
import PlacePhoto from './PlacePhoto'

export default function AllPlaces({ places = [], tourStops = [], loading = false, error = '', showOnMap = false, onToggleMap, language = 'en', cityName = '', onPlaceSelect }) {
  const t = getTranslations(language)
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('ALL')
  const [indoor, setIndoor] = useState('ALL')
  const [filtersOpen, setFiltersOpen] = useState(false)
  const selected = useMemo(() => new Set(tourStops.map((item) => item.placeId)), [tourStops])
  const filtered = places.filter((place) => place.name.toLocaleLowerCase().includes(query.trim().toLocaleLowerCase()) && (category === 'ALL' || place.category === category) && (indoor === 'ALL' || (indoor === 'INDOOR') === place.indoor))
  return <div className="places-layout">
    <div className="places-toolbar">
      <div className="places-headline"><span className="heading-icon"><MapPin size={22} aria-hidden="true" /></span><div><h2>{language === 'fi' ? 'Kaikki kohteet' : 'All places'}{cityName ? ` ${language === 'fi' ? 'kaupungissa' : 'in'} ${cityName}` : ''}</h2><p>{t.allPlacesDescription}</p></div></div>
      <div className="places-actions"><label className="places-search"><Search size={15} aria-hidden="true" /><input aria-label={t.searchPlaces} type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder={language === 'fi' ? 'Hae kohteita...' : 'Search places (museum, nature...)'} /></label><button className={`filter-button ${filtersOpen ? 'active' : ''}`} type="button" aria-expanded={filtersOpen} onClick={() => setFiltersOpen(v => !v)}><SlidersHorizontal size={15} aria-hidden="true" />{language === 'fi' ? 'Suodattimet' : 'Filters'}{(category !== 'ALL' || indoor !== 'ALL') && <span className="filter-dot" />}</button></div>
    </div>
    {filtersOpen && <div className="places-filters"><label>{t.filterCategory}<select value={category} onChange={(e) => setCategory(e.target.value)}><option value="ALL">{t.allCategories}</option>{['MUSEUM', 'LANDMARK', 'NATURE', 'OTHER'].map((key) => <option key={key} value={key}>{t[`category${key}`]}</option>)}</select></label><label>{language === 'fi' ? 'Tyyppi' : 'Type'}<select value={indoor} onChange={(e) => setIndoor(e.target.value)}><option value="ALL">{language === 'fi' ? 'Kaikki' : 'All'}</option><option value="INDOOR">{t.indoor}</option><option value="OUTDOOR">{t.outdoor}</option></select></label><label className="filter-check"><input type="checkbox" checked={showOnMap} disabled={loading || !!error || !places.length} onChange={e => onToggleMap(e.target.checked)} />{t.showAllOnMap}</label></div>}
    <div className="place-cards-scroll">
      {loading ? <p className="places-message">{t.placesLoading}</p> : error ? <p className="places-message" role="alert">{t.placesLoadFailed}: {error}</p> : !places.length ? <p className="places-message">{t.placesEmpty}</p> : !filtered.length ? <p className="places-message">{t.noMatchingPlaces}</p> : <div className="place-cards">
        {filtered.map((place) => <article key={place.id} className="place-card">
          <button className="place-cover" type="button" onClick={() => onPlaceSelect?.(place)} aria-label={`${place.name} — ${language === 'fi' ? 'Näytä kartalla' : 'View on map'}`}><PlacePhoto category={place.category} name={place.name}/><span className="place-explore" aria-hidden="true"><MapPin size={14} /></span></button>
          <div className="place-card-content"><button type="button" className="place-title" onClick={() => onPlaceSelect?.(place)} title={place.name}>{place.name}</button><div className="place-chips"><span>{t[`category${place.category}`] ?? place.category}</span><span className={place.indoor ? 'inside' : 'outside'}>{place.indoor ? t.indoor : t.outdoor}</span></div><div className="place-card-bottom">{selected.has(place.id) ? <span className="in-tour"><Check size={12} aria-hidden="true" />{t.inYourTour}</span> : <span><Clock size={12} aria-hidden="true" />{place.visitMinutes} min</span>}{place.link && <a href={place.link} target="_blank" rel="noreferrer">{language === 'fi' ? 'Lisätiedot' : 'Details'} <ExternalLink size={11} aria-hidden="true" /></a>}</div></div>
        </article>)}
      </div>}
    </div>
  </div>
}
