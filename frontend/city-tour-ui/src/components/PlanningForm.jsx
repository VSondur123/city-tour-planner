import { useState } from 'react'

// Cities are examples. In the real app the list should come from the cities
// that the places provider actually covers (see the design document, section 2.3).
const CITIES = ['Helsinki', 'Espoo', 'Vantaa']
const DURATIONS = [2, 4, 6]

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function PlanningForm({ onGenerate, loading }) {
  const [city, setCity] = useState(CITIES[0])
  const [date, setDate] = useState(todayIso())
  const [time, setTime] = useState('13:00')
  const [duration, setDuration] = useState(2)

  function handleSubmit(e) {
    e.preventDefault()
    onGenerate({ city, date, time, duration })
  }

  const field = 'h-12 rounded-lg border border-slate-400 bg-white px-3 text-base'

  return (
    <form onSubmit={handleSubmit} className="space-y-5 rounded-2xl border border-slate-300 bg-white p-8">
      <h2 className="text-xl font-bold">Tour settings</h2>

      <div className="flex flex-col gap-2">
        <label htmlFor="city" className="font-bold">City</label>
        <select id="city" value={city} onChange={(e) => setCity(e.target.value)} className={field}>
          {CITIES.map((c) => <option key={c}>{c}</option>)}
        </select>
      </div>

      <div className="flex flex-wrap gap-4">
        <div className="flex flex-1 flex-col gap-2">
          <label htmlFor="date" className="font-bold">Date</label>
          <input id="date" type="date" value={date} onChange={(e) => setDate(e.target.value)} className={field} />
        </div>
        <div className="flex flex-1 flex-col gap-2">
          <label htmlFor="time" className="font-bold">Start time</label>
          <input id="time" type="time" value={time} onChange={(e) => setTime(e.target.value)} className={field} />
        </div>
      </div>

      <fieldset>
        <legend className="mb-2 font-bold">Duration</legend>
        <div className="flex gap-3">
          {DURATIONS.map((d) => (
            <label
              key={d}
              className={`flex h-12 flex-1 cursor-pointer items-center justify-center gap-2 rounded-lg border ${
                duration === d ? 'border-2 border-teal-700 bg-teal-50 font-bold' : 'border-slate-400'
              }`}
            >
              <input type="radio" name="duration" checked={duration === d} onChange={() => setDuration(d)} className="accent-teal-700" />
              {d} h
            </label>
          ))}
        </div>
      </fieldset>

      <button
        type="submit"
        disabled={loading}
        className="h-13 w-full rounded-xl bg-teal-700 py-3 text-lg font-bold text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {loading ? 'Building your tour...' : 'Generate tour'}
      </button>
    </form>
  )
}