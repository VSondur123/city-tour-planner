// SAMPLE DATA for UI development only. Replace with backend responses.
export const mockTour = {
  stops: [
    { id: 1, name: 'Ateneum Art Museum', lat: 60.1699, lng: 24.9439, time: '13:00–13:40' },
    { id: 2, name: 'Kiasma', lat: 60.1703, lng: 24.9370, time: '13:45–14:25' },
    { id: 3, name: 'Helsinki Cathedral', lat: 60.1704, lng: 24.9522, time: '14:35–14:55' },
  ],
  weather: [
    { hour: '12', temp: 6, rain: 0.8 },
    { hour: '13', temp: 6, rain: 1.2 },
    { hour: '14', temp: 5, rain: 1.5 },
    { hour: '15', temp: 5, rain: 1.0 },
  ],
    traffic: [
    {
      id: 't1',
      type: 'Road work',
      description: 'Lane closed',
      area: 'Central Helsinki',
      // GeoJSON order: [lng, lat]. This is what Digitraffic returns.
      line: [[24.94, 60.168], [24.945, 60.169], [24.951, 60.1695]],
    },
    {
      id: 't2',
      type: 'Disruption',
      description: 'Reduced speed limit',
      area: 'Ring road',
      line: [[24.90, 60.175], [24.91, 60.178]],
    },
  ],
}