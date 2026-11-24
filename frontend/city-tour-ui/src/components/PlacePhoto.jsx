const images = {
  NATURE: 'https://images.unsplash.com/photo-1448375240586-882707db888b?w=700&q=80',
  MUSEUM: 'https://images.unsplash.com/photo-1561214115-f2f134cc4912?w=700&q=80',
  LANDMARK: 'https://images.unsplash.com/photo-1519501025264-65ba15a82390?w=700&q=80',
  OTHER: 'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?w=700&q=80',
}
export default function PlacePhoto({ category = 'OTHER', name = '', className = '' }) {
  return <div className={`place-photo place-photo-${category.toLowerCase()} ${className}`}>
    <img loading="lazy" src={images[category] ?? images.OTHER} alt="" onError={(event) => { event.currentTarget.style.display = 'none' }} />
    <span aria-hidden="true" className="place-photo-symbol">{category === 'NATURE' ? '♣' : category === 'LANDMARK' ? '⌖' : category === 'MUSEUM' ? '▤' : '◈'}</span>
    <span className="sr-only">{name}</span>
  </div>
}
