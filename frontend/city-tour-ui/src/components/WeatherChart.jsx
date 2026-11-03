import { ComposedChart, Bar, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

export default function WeatherChart({ data }) {
  return (
    <div className="h-48 w-full">
      <ResponsiveContainer>
        <ComposedChart data={data}>
          <XAxis dataKey="hour" />
          <YAxis yAxisId="temp" />
          <YAxis yAxisId="rain" orientation="right" />
          <Tooltip />
          <Bar yAxisId="rain" dataKey="rain" fill="#9CC3D5" />
          <Line yAxisId="temp" dataKey="temp" type="monotone" stroke="#B45309" strokeWidth={3} />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  )
}