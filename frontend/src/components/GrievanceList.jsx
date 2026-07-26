import { useState, useEffect, useCallback } from 'react'
import { fetchGrievances, fetchMeta } from '../api'

export default function GrievanceList({ refreshKey }) {
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [statuses, setStatuses] = useState([])

  const [results, setResults] = useState([])
  const [count, setCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchMeta().then((data) => setStatuses(data.statuses || [])).catch(() => {})
  }, [])

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    fetchGrievances({ search, status })
      .then((data) => {
        setResults(data.results || [])
        setCount(data.count || 0)
      })
      .catch(() => setError('Could not load grievances. Is the backend running on port 8080?'))
      .finally(() => setLoading(false))
  }, [search, status])

  useEffect(() => {
    load()
  }, [load, refreshKey])

  return (
    <div className="card">
      <h2>Grievance Register</h2>

      <div className="filters">
        <input
          placeholder="Search by name, id or description..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {statuses.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      {loading && <p>Loading grievances...</p>}
      {error && <p className="error-text">{error}</p>}
      {!loading && !error && results.length === 0 && (
        <p>No grievances match your search/filter.</p>
      )}

      {!loading && !error && results.length > 0 && (
        <>
          <p className="result-count">Showing {count} grievance{count === 1 ? '' : 's'}</p>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Complainant</th>
                <th>Ward</th>
                <th>Category</th>
                <th>Department</th>
                <th>Status</th>
                <th>Days waiting / to resolve</th>
                <th>Delay risk</th>
              </tr>
            </thead>
            <tbody>
              {results.map((g) => (
                <tr key={g.grievanceId}>
                  <td>{g.grievanceId}</td>
                  <td>{g.complainant}</td>
                  <td>{g.ward}</td>
                  <td>{g.category}</td>
                  <td>{g.department || <span className="muted">Not assigned</span>}</td>
                  <td><span className={`badge status-${g.status.replace(' ', '-').toLowerCase()}`}>{g.status}</span></td>
                  <td>{g.daysOpen ?? '—'}</td>
                  <td>
                    {g.predictedDelayRisk
                      ? `${g.predictedDelayRisk} (${Math.round(g.predictionConfidence * 100)}%)`
                      : <span className="muted">Uncertain</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  )
}
