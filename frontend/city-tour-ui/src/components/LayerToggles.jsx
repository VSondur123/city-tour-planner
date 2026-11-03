export default function LayerToggles({ layers, onToggle }) {
  const items = [
    { key: 'weather', label: 'Weather' },
    { key: 'traffic', label: 'Traffic' },
    { key: 'places', label: 'Places' },
  ]
  return (
    <div className="rounded-xl border border-slate-300 bg-white p-5">
      <h2 className="mb-3 text-lg font-bold">Data layers</h2>
      {items.map(({ key, label }) => (
        <button
          key={key}
          type="button"
          role="switch"
          aria-checked={layers[key]}
          onClick={() => onToggle(key)}
          className="flex h-12 w-full items-center justify-between"
        >
          <span>{label}</span>
          <span className={`flex h-7 w-12 items-center rounded-full p-0.5 ${layers[key] ? 'justify-end bg-teal-700' : 'justify-start bg-slate-400'}`}>
            <span className="h-6 w-6 rounded-full bg-white" />
          </span>
        </button>
      ))}
      <p className="mt-2 text-sm text-slate-600">Switching a layer updates the view instantly, with no new API calls.</p>
    </div>
  )
}