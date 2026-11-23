import { getTranslations } from '../i18n/translations'

function formatDateTime(value, language) {
  if (!value) return null
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return null
  return new Intl.DateTimeFormat(language === 'fi' ? 'fi-FI' : 'en-GB', {
    timeZone: 'Europe/Helsinki', day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  }).format(date)
}

export default function TrafficPanel({ announcements = [], error = false, language = 'en' }) {
  const t = getTranslations(language)
  if (error) {
    return <div role="alert" className="rounded-xl border border-amber-300 bg-amber-50 p-4">
      <strong className="text-amber-900">{t.trafficUnavailableTitle}</strong>
      <p className="mt-1 text-sm text-amber-800">{t.trafficUnavailableDescription}</p>
    </div>
  }
  return (
    <section className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-2 text-lg font-bold">{t.trafficTitle}</h2>
      <p className="mb-2 text-sm text-slate-600">{t.trafficCount(announcements.length)}</p>
      {announcements.length > 0 && <p className="mb-4 text-xs text-slate-500">{t.sourceLanguageNotice}</p>}
      {announcements.length === 0 ? (
        <p className="rounded-lg bg-slate-50 p-4 text-sm text-slate-600">{t.trafficEmpty}</p>
      ) : (
        <div className="space-y-4">
          {announcements.map((item, index) => (
            <article key={`${item.type}-${item.startTime}-${index}`} className="rounded-lg border border-slate-200 p-4">
              <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-900">{t.trafficNotice} {index + 1}</span>
                <span className="rounded-full bg-slate-100 px-2 py-1 text-xs text-slate-700">{item.language?.toUpperCase() ?? t.unknownLanguage}</span>
              </div>
              <h3 className="font-bold text-slate-800">{item.type}</h3>
              <p className="mt-2 text-sm text-slate-700">{item.description}</p>
              <dl className="mt-3 grid grid-cols-1 gap-2 text-xs sm:grid-cols-2">
                <div><dt className="font-semibold text-slate-600">{t.area}</dt><dd className="text-slate-800">{item.area || t.unknown}</dd></div>
                <div><dt className="font-semibold text-slate-600">{t.status}</dt><dd className="text-slate-800">{item.endTime == null ? t.noScheduledEnd : t.scheduledEnd}</dd></div>
                <div><dt className="font-semibold text-slate-600">{t.start}</dt><dd className="text-slate-800">{formatDateTime(item.startTime, language) ?? t.unknown}</dd></div>
                <div><dt className="font-semibold text-slate-600">{t.end}</dt><dd className="text-slate-800">{formatDateTime(item.endTime, language) ?? t.notSpecified}</dd></div>
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
