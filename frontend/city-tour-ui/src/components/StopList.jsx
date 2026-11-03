export default function StopList({ stops }) {
  return (
    <div className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-3 text-lg font-bold">Stops</h2>
      <ol className="space-y-3">
        {stops.map((s) => (
          <li key={s.id} className="flex items-start gap-3">
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-teal-700 font-bold text-white">
              {s.id}
            </span>
            <div>
              <div className="font-bold">{s.name}</div>
              <div className="text-sm text-slate-600">{s.time}</div>
            </div>
          </li>
        ))}
      </ol>
    </div>
  )
}