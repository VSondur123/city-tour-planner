import { getTranslations } from '../i18n/translations'

export default function StopList({ stops = [], language = 'en' }) {
  const t = getTranslations(language)
  return (
    <section className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-4 text-lg font-bold">{t.tourStops} ({stops.length})</h2>
      {stops.length === 0 ? (
        <p className="text-sm text-slate-600">{t.noStopsList}</p>
      ) : (
        <ol className="list-none space-y-4 p-0">
          {stops.map((stop) => (
            <li key={stop.placeId} className="flex items-start gap-3 border-b border-slate-100 pb-4 last:border-0 last:pb-0">
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-teal-700 font-bold text-white">{stop.id}</span>
              <div className="min-w-0 flex-1">
                <h3 className="font-bold text-slate-800">{stop.name}</h3>
                <p className="mt-1 text-sm font-medium text-teal-800">{stop.time}</p>
                <div className="mt-2 flex flex-wrap gap-2">
                  <span className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-700">{t[`category${stop.category}`] ?? stop.category}</span>
                  <span className={`rounded-full px-2 py-1 text-xs ${stop.indoor ? 'bg-blue-50 text-blue-800' : 'bg-green-50 text-green-800'}`}>{stop.indoor ? t.indoor : t.outdoor}</span>
                </div>
                <p className="mt-2 text-xs text-slate-500">{t.visitDuration}: {stop.visitMinutes} {t.minutes}</p>
                {stop.link && <a href={stop.link} target="_blank" rel="noopener noreferrer" className="mt-2 inline-block text-sm font-medium text-teal-700 underline hover:text-teal-900">{t.morePlace}</a>}
              </div>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
