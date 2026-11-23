
import { useEffect, useState } from 'react'
import { getCities } from '../api/tourApi'

const DURATIONS = [2, 4, 6]
const HELSINKI_TIME_ZONE = 'Europe/Helsinki'

function todayIso() {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: HELSINKI_TIME_ZONE,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date())
}

export default function PlanningForm({ onGenerate, loading }) {
  const [cities, setCities] = useState([])
  const [city, setCity] = useState('')
  const [citiesLoading, setCitiesLoading] = useState(true)
  const [citiesError, setCitiesError] = useState('')

  const [date, setDate] = useState(todayIso())
  const [time, setTime] = useState('13:00')
  const [duration, setDuration] = useState(2)

  useEffect(() => {
    const controller = new AbortController()
    let active = true

    async function loadCities() {
      try {
        setCitiesLoading(true)
        setCitiesError('')

        const result = await getCities()

        if (!active) return

        setCities(result)
        setCity(result[0]?.id ?? '')
      } catch (error) {
        if (!active) return

        console.error('Failed to load cities:', error)
        setCitiesError(error.message)
      } finally {
        if (active) setCitiesLoading(false)
      }
    }

    loadCities()

    return () => {
      active = false
      controller.abort()
    }
  }, [])

  function handleSubmit(event) {
    event.preventDefault()

    if (!city || loading || citiesLoading) return

    onGenerate({
      city,
      date,
      startTime: time,
      durationHours: duration,
      language: 'en',
      factors: ['WEATHER', 'TRAFFIC'],
    })
  }

  const field =
    'h-12 rounded-lg border border-slate-400 bg-white px-3 text-base'

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-5 rounded-2xl border border-slate-300 bg-white p-8"
    >
      <h2 className="text-xl font-bold">
        Tour settings
      </h2>

      <div className="flex flex-col gap-2">
        <label htmlFor="city" className="font-bold">
          City
        </label>

        <select
          id="city"
          value={city}
          onChange={(event) => setCity(event.target.value)}
          disabled={citiesLoading || Boolean(citiesError)}
          className={field}
        >
          {citiesLoading && (
            <option value="">Loading cities...</option>
          )}

          {!citiesLoading && cities.length === 0 && (
            <option value="">No cities available</option>
          )}

          {cities.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>

        {citiesError && (
          <p role="alert" className="text-sm text-red-700">
            Failed to load cities: {citiesError}
          </p>
        )}
      </div>

      <div className="flex flex-wrap gap-4">
        <div className="flex flex-1 flex-col gap-2">
          <label htmlFor="date" className="font-bold">
            Date
          </label>

          <input
            id="date"
            type="date"
            min={todayIso()}
            value={date}
            onChange={(event) => setDate(event.target.value)}
            required
            className={field}
          />
        </div>

        <div className="flex flex-1 flex-col gap-2">
          <label htmlFor="time" className="font-bold">
            Start time
          </label>

          <input
            id="time"
            type="time"
            value={time}
            onChange={(event) => setTime(event.target.value)}
            required
            className={field}
          />
        </div>
      </div>

      <fieldset>
        <legend className="mb-2 font-bold">
          Duration
        </legend>

        <div className="flex gap-3">
          {DURATIONS.map((hours) => (
            <label
              key={hours}
              className={`flex h-12 flex-1 cursor-pointer items-center justify-center gap-2 rounded-lg border ${
                duration === hours
                  ? 'border-2 border-teal-700 bg-teal-50 font-bold'
                  : 'border-slate-400'
              }`}
            >
              <input
                type="radio"
                name="duration"
                checked={duration === hours}
                onChange={() => setDuration(hours)}
                className="accent-teal-700"
              />
              {hours} h
            </label>
          ))}
        </div>
      </fieldset>

      <button
        type="submit"
        disabled={loading || citiesLoading || !city}
        className="w-full rounded-xl bg-teal-700 py-3 text-lg font-bold text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {loading ? 'Building your tour...' : 'Generate tour'}
      </button>
    </form>
  )
}
