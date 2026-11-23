import { ComposedChart, Bar, Line, CartesianGrid, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { getTranslations } from '../i18n/translations'

function WeatherTooltip({ active, payload, label, language }) {
  if (!active || !payload?.length) return null
  const weather = payload[0]?.payload
  if (!weather) return null
  const t = getTranslations(language)
  const display = (value, unit) => value == null ? t.noValue : `${value} ${unit}`
  return <div className="rounded-lg border border-slate-200 bg-white p-3 text-sm shadow-lg">
    <p className="mb-2 font-bold text-slate-800">{label}</p>
    <p className="text-orange-700">{t.temperature}: {display(weather.temp, '°C')}</p>
    <p className="text-blue-700">{t.rain}: {display(weather.rain, 'mm')}</p>
    <p className="text-slate-700">{t.cloudCover}: {display(weather.cloudCover, '%')}</p>
    <p className="text-slate-700">{t.wind}: {display(weather.wind, 'm/s')}</p>
  </div>
}

export default function WeatherChart({ data = [], language = 'en' }) {
  const t = getTranslations(language)
  if (data.length === 0) return <p className="py-8 text-center text-sm text-slate-500">{t.noHourlyWeather}</p>
  return <div className="w-full min-w-0">
    <p className="mb-3 text-sm text-slate-600">{t.weatherChartTitle}</p>
    <div className="h-72 w-full min-w-0">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={data} margin={{ top: 15, right: 10, left: 0, bottom: 15 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
          <XAxis dataKey="hour" tick={{ fontSize: 11 }} minTickGap={24} tickMargin={8} />
          <YAxis yAxisId="temp" orientation="left" tick={{ fontSize: 11 }} tickFormatter={(value) => `${value}°`} width={45} domain={['auto', 'auto']} />
          <YAxis yAxisId="rain" orientation="right" tick={{ fontSize: 11 }} width={43} domain={[0, 'auto']} allowDecimals />
          <Tooltip content={<WeatherTooltip language={language} />} />
          <Legend verticalAlign="bottom" height={30} />
          <Bar yAxisId="rain" dataKey="rain" name={t.rainLegend} fill="#60a5fa" maxBarSize={22} />
          <Line yAxisId="temp" dataKey="temp" name={t.tempLegend} type="monotone" stroke="#ea580c" strokeWidth={3} dot={false} activeDot={{ r: 5 }} connectNulls={false} />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
    <p className="mt-2 text-xs text-slate-500">{t.weatherChartHint}</p>
  </div>
}
