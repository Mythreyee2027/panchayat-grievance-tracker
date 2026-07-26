const BASE_URL = 'http://localhost:8080/api/grievances'

export async function fetchMeta() {
  const res = await fetch(`${BASE_URL}/meta`)
  if (!res.ok) throw new Error('Failed to load form options')
  return res.json()
}

export async function fetchGrievances({ search = '', status = '', category = '', department = '' } = {}) {
  const params = new URLSearchParams({ search, status, category, department })
  const res = await fetch(`${BASE_URL}?${params.toString()}`)
  if (!res.ok) throw new Error('Failed to load grievances')
  return res.json()
}

export async function createGrievance(payload) {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    const errorBody = await res.json().catch(() => ({}))
    const err = new Error('Validation failed')
    err.fieldErrors = errorBody
    throw err
  }
  return res.json()
}
