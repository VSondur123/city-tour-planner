import { getTranslations } from '../i18n/translations'

export default function LayerToggles({ layers, onToggle, language = 'en' }) {
  const t = getTranslations(language)
  const items = [
    { key: 'weather', label: t.weather },
    { key: 'traffic', label: t.traffic },
    { key: 'places', label: t.places },
  ]
  return (
    <section className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-3 text-lg font-bold">{t.dataLayers}</h2>
      {items.map(({ key, label }) => (
        <button key={key} type="button" role="switch" aria-checked={layers[key]} onClick={() => onToggle(key)} className="flex h-12 w-full items-center justify-between">
          <span>{label}</span>
          <span className={`flex h-7 w-12 items-center rounded-full p-0.5 ${layers[key] ? 'justify-end bg-teal-700' : 'justify-start bg-slate-400'}`}>
            <span className="h-6 w-6 rounded-full bg-white" />
          </span>
        </button>
      ))}
      <p className="mt-2 text-sm text-slate-600">{t.layersHint}</p>
    </section>
  )
}
