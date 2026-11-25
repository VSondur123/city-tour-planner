
const API_BASE = '/api'

async function apiRequest(path, options = {}) {
  let response

  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        Accept: 'application/json',
        ...options.headers,
      },
    })
  } catch {
    throw new Error(
      'Cannot connect to the backend. Check that Spring Boot is running.'
    )
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    const error = new Error(
      `API request failed with HTTP ${response.status}`
    )

    error.status = response.status
    error.code = data?.code ?? 'UNKNOWN_ERROR'
    error.fields = data?.errors ?? []

    throw error
  }

  return data
}

export function getCities() {
  return apiRequest('/cities')
}

export function getPlaces(city, language = 'en') {
  const query = new URLSearchParams({ city, language })
  return apiRequest(`/places?${query}`)
}

export function generateTour(request) {
  return apiRequest('/tour', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
}
