
import {
  ComposedChart,
  Bar,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'

function WeatherTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null

  const weather = payload[0]?.payload

  if (!weather) return null

  function display(value, unit) {
    return value == null ? 'N/A' : `${value} ${unit}`
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-3 text-sm shadow-lg">
      <p className="mb-2 font-bold text-slate-800">
        {label}
      </p>

      <p className="text-orange-700">
        Temperature: {display(weather.temp, '°C')}
      </p>

      <p className="text-blue-700">
        Precipitation: {display(weather.rain, 'mm')}
      </p>

      <p className="text-slate-700">
        Cloud cover: {display(weather.cloudCover, '%')}
      </p>

      <p className="text-slate-700">
        Wind: {display(weather.wind, 'm/s')}
      </p>
    </div>
  )
}

export default function WeatherChart({ data = [] }) {
  if (data.length === 0) {
    return (
      <p className="py-8 text-center text-sm text-slate-500">
        No hourly weather data available.
      </p>
    )
  }

  return (
    <div className="w-full min-w-0">
      <p className="mb-3 text-sm text-slate-600">
        Hourly temperature and precipitation forecast
      </p>

      <div className="h-72 w-full min-w-0">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart
            data={data}
            margin={{
              top: 15,
              right: 10,
              left: 0,
              bottom: 15,
            }}
          >
            <CartesianGrid
              strokeDasharray="3 3"
              stroke="#e2e8f0"
            />

            <XAxis
              dataKey="hour"
              tick={{ fontSize: 11 }}
              minTickGap={24}
              tickMargin={8}
            />

            <YAxis
              yAxisId="temp"
               orientation="left"
              tick={{ fontSize: 11 }}
              tickFormatter={(value) => `${value}°`}
              width={45}
              domain={['auto', 'auto']}
            />

            <YAxis
              yAxisId="rain"
              orientation="right"
              tick={{ fontSize: 11 }}
              tickFormatter={(value) => `${value}`}
              width={35}
              domain={[0, 'auto']}
              allowDecimals
            />

            <Tooltip content={<WeatherTooltip />} />

            <Legend verticalAlign="bottom" height={30} />

            <Bar
              yAxisId="rain"
              dataKey="rain"
              name="Rain (mm)"
              fill="#60a5fa"
              maxBarSize={22}
            />

            <Line
              yAxisId="temp"
              dataKey="temp"
              name="Temperature (°C)"
              type="monotone"
              stroke="#ea580c"
              strokeWidth={3}
              dot={false}
              activeDot={{ r: 5 }}
              connectNulls={false}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      <p className="mt-2 text-xs text-slate-500">
        Rainfall is measured in millimetres per hour.
        Hover over the chart to see temperature,
        rainfall, wind speed and cloud cover.
      </p>
    </div>
  )
}
