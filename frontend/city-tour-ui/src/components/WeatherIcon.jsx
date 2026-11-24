import { Cloud, CloudDrizzle, CloudRain, CloudSun, Sun } from 'lucide-react'

/** Picks a weather icon from the forecast values (rain amount in mm, cloud cover in %). */
export default function WeatherIcon({ rain, cloudCover, size = 26 }) {
  if (rain != null && rain >= 0.5) return <CloudRain size={size} aria-hidden="true" />
  if (rain != null && rain > 0.05) return <CloudDrizzle size={size} aria-hidden="true" />
  if (cloudCover == null) return <Cloud size={size} aria-hidden="true" />
  if (cloudCover < 25) return <Sun size={size} aria-hidden="true" />
  if (cloudCover < 60) return <CloudSun size={size} aria-hidden="true" />
  return <Cloud size={size} aria-hidden="true" />
}
