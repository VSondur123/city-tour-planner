import { getTranslations } from '../i18n/translations'

export default function LayerToggles({ layers, onToggle, language = 'en' }) {
  const t = getTranslations(language)
  const items = [
    { key: 'weather', label: t.weather },
    { key: 'traffic', label: t.traffic },
    { key: 'places', label: t.places },
  ]
  return (
    <section className="ct-layer-toggles">
      <h2 className="sr-only">{t.dataLayers}</h2>
      {items.map(({ key, label }) => (
        <label key={key} className="ct-layer-row">
          <span>{label}</span>
          <input type="checkbox" role="switch" checked={layers[key]} onChange={() => onToggle(key)} />
        </label>
      ))}
      <p>{t.layersHint}</p>
    </section>
  )
}
