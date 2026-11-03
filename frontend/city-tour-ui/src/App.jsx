import { useState } from 'react'
import MapView from './components/MapView'
import WeatherChart from './components/WeatherChart'
import { mockTour } from './data/mockTour'

export default function App() {
  const [showWeather, setShowWeather] = useState(true)
  const [showPlaces, setShowPlaces] = useState(true)

  return (
    <div className="min-h-screen bg-slate-100 p-8">
      <h1 className="text-3xl font-semibold text-teal-700">City Tour Planner</h1>

      <div className="mt-4 flex gap-4">
        <label><input type="checkbox" checked={showWeather} onChange={() => setShowWeather(!showWeather)} /> Weather</label>
        <label><input type="checkbox" checked={showPlaces} onChange={() => setShowPlaces(!showPlaces)} /> Places</label>
      </div>

      <div className="mt-4 space-y-6">
        {showPlaces && <MapView stops={mockTour.stops} />}
        {showWeather && <WeatherChart data={mockTour.weather} />}
      </div>
    </div>
  )
}