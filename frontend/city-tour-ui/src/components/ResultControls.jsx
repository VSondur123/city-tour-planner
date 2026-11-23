import { getTranslations } from '../i18n/translations'

export default function ResultControls({ request, loading, onChange }) {
  const t = getTranslations(request.language)
  const weatherOn = request.factors.includes('WEATHER')
  const trafficOn = request.factors.includes('TRAFFIC')

  function changeLanguage(language) {
    if (!loading && language !== request.language) {
      onChange({ ...request, language })
    }
  }

  function changeFactor(factor, enabled) {
    if (loading) return
    const factors = enabled
      ? [...request.factors.filter((item) => item !== factor), factor]
      : request.factors.filter((item) => item !== factor)
    onChange({ ...request, factors })
  }

  return (
    <section className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-3 text-lg font-bold">{t.planningOptions}</h2>
      <p className="mb-4 text-xs text-slate-600">{t.planningHint}</p>
      <label htmlFor="result-language" className="mb-2 block text-sm font-semibold">{t.language}</label>
      <select id="result-language" value={request.language} onChange={(e) => changeLanguage(e.target.value)} disabled={loading} className="mb-4 h-11 w-full rounded-lg border border-slate-300 bg-white px-3">
        <option value="en">{t.english}</option>
        <option value="fi">{t.finnish}</option>
      </select>
      <fieldset className="space-y-3">
        <legend className="mb-2 text-sm font-semibold">{t.planningFactors}</legend>
        <label className="flex cursor-pointer items-center gap-3">
          <input type="checkbox" checked={weatherOn} disabled={loading} onChange={(e) => changeFactor('WEATHER', e.target.checked)} className="h-5 w-5 accent-teal-700" />
          <span className="text-sm">{t.useWeather}</span>
        </label>
        <label className="flex cursor-pointer items-center gap-3">
          <input type="checkbox" checked={trafficOn} disabled={loading} onChange={(e) => changeFactor('TRAFFIC', e.target.checked)} className="h-5 w-5 accent-teal-700" />
          <span className="text-sm">{t.useTraffic}</span>
        </label>
      </fieldset>
      {loading && <p role="status" className="mt-4 text-sm font-medium text-teal-700">{t.regenerating}</p>}
      <p className="mt-4 text-xs text-slate-500">{t.factorsHint}</p>
    </section>
  )
}
