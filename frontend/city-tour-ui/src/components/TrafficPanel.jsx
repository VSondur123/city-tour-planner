export default function TrafficPanel({ announcements, error }) {
  if (error) {
    return (
      <div role="alert" className="rounded-xl border border-amber-700 bg-amber-50 p-4">
        <strong>Digitraffic road announcements are unavailable.</strong> Showing the tour without traffic information.
      </div>
    )
  }

  return (
    <div className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-3 text-lg font-bold">Road traffic announcements</h2>
      {announcements.length === 0 ? (
        <p className="text-sm text-slate-600">No announcements for this area.</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-slate-600">
              <th className="py-1">Type</th>
              <th className="py-1">Description</th>
              <th className="py-1">Area</th>
            </tr>
          </thead>
          <tbody>
            {announcements.map((a) => (
              <tr key={a.id} className="border-t border-slate-300">
                <td className="py-2">{a.type}</td>
                <td>{a.description}</td>
                <td>{a.area}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}