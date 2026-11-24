import { useMemo, useState } from 'react'
import { getTranslations } from '../i18n/translations'

const CATEGORIES = ['ALL', 'MUSEUM', 'LANDMARK', 'NATURE', 'OTHER']

export default function AllPlaces({
  places = [], tourStops = [], loading = false, error = '', showOnMap = false,
  onToggleMap, language = 'en',
}) {
  const t = getTranslations(language)
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('ALL')
  const selectedIds = useMemo(() => new Set(tourStops.map((stop) => stop.placeId)), [tourStops])
  const filtered = places.filter((place) => {
    const matchesName = place.name.toLocaleLowerCase().includes(query.trim().toLocaleLowerCase())
    return matchesName && (category === 'ALL' || place.category === category)
  })
  const categoryName = (value) => t[`category${value}`] ?? value

  return (
    <section className="rounded-xl border border-slate-300 bg-white p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-bold">{t.allPlacesTitle} ({places.length})</h2>
          <p className="mt-1 text-sm text-slate-600">{t.allPlacesDescription}</p>
        </div>
        <label className="flex cursor-pointer items-center gap-2 text-sm font-medium">
          <input type="checkbox" className="h-5 w-5 accent-teal-700" checked={showOnMap}
            onChange={(event) => onToggleMap(event.target.checked)} disabled={loading || Boolean(error) || !places.length} />
          {t.showAllOnMap}
        </label>
      </div>
      {loading ? <p role="status" className="mt-3 text-sm text-slate-600">{t.placesLoading}</p> :
        error ? <p role="alert" className="mt-3 text-sm text-amber-800">{t.placesLoadFailed}: {error}</p> :
        places.length === 0 ? <p className="mt-3 text-sm text-slate-600">{t.placesEmpty}</p> : <>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <label className="block text-sm">
              <span className="mb-1 block font-semibold">{t.searchPlaces}</span>
              <input type="search" className="w-full rounded-lg border border-slate-300 px-3 py-2"
                value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t.searchPlaceholder} />
            </label>
            <label className="block text-sm">
              <span className="mb-1 block font-semibold">{t.filterCategory}</span>
              <select className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                value={category} onChange={(event) => setCategory(event.target.value)}>
                {CATEGORIES.map((item) => <option key={item} value={item}>
                  {item === 'ALL' ? t.allCategories : categoryName(item)}
                </option>)}
              </select>
            </label>
          </div>
          <p className="my-3 text-xs text-slate-600">{t.placesShown(filtered.length, places.length)}</p>
          {filtered.length === 0 ? <p className="text-sm text-slate-600">{t.noMatchingPlaces}</p> :
            <ul className="max-h-96 space-y-2 overflow-y-auto pr-1">
              {filtered.map((place) => <li key={place.id} className="rounded-lg border border-slate-200 p-3">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-semibold">{place.name}</span>
                  {selectedIds.has(place.id) && <span className="rounded-full bg-teal-50 px-2 py-1 text-xs font-semibold text-teal-800">{t.inYourTour}</span>}
                </div>
                <div className="mt-2 flex flex-wrap gap-2 text-xs text-slate-600">
                  <span>{categoryName(place.category)}</span>
                  <span>· {place.indoor ? t.indoor : t.outdoor}</span>
                  <span>· {place.visitMinutes} {t.minutes}</span>
                </div>
                <div className="mt-1 text-xs text-slate-500">{t.coordinates}: {place.lat}, {place.lon}</div>
                {place.link && <a href={place.link} target="_blank" rel="noopener noreferrer"
                  className="mt-2 inline-block text-sm text-teal-700 underline">{t.morePlace}</a>}
              </li>)}
            </ul>}
        </>}
    </section>
  )
}
