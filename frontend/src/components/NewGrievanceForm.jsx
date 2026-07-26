import { useState, useEffect } from 'react'
import { fetchMeta, createGrievance } from '../api'

export default function NewGrievanceForm({ onCreated }) {
  const [categories, setCategories] = useState([])
  const [metaError, setMetaError] = useState('')

  const [form, setForm] = useState({ complainant: '', ward: '', category: '', description: '' })
  const [fieldErrors, setFieldErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')

  useEffect(() => {
    fetchMeta()
      .then((data) => setCategories(data.categories || []))
      .catch(() => setMetaError('Could not load categories. Is the backend running on port 8080?'))
  }, [])

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setFieldErrors({})
    setSuccessMsg('')
    try {
      const payload = { ...form, ward: form.ward ? Number(form.ward) : null }
      const created = await createGrievance(payload)
      setSuccessMsg(`Grievance ${created.grievanceId} recorded successfully.`)
      setForm({ complainant: '', ward: '', category: '', description: '' })
      if (onCreated) onCreated()
    } catch (err) {
      if (err.fieldErrors) setFieldErrors(err.fieldErrors)
      else setFieldErrors({ _general: 'Could not save the grievance. Is the backend running?' })
    } finally {
      setSubmitting(false)
    }
  }

  if (metaError) return <p className="error-text">{metaError}</p>

  return (
    <form className="card" onSubmit={handleSubmit}>
      <h2>Record a New Grievance</h2>

      {successMsg && <p className="success-text">{successMsg}</p>}
      {fieldErrors._general && <p className="error-text">{fieldErrors._general}</p>}

      <label>
        Complainant name
        <input name="complainant" value={form.complainant} onChange={handleChange} />
      </label>
      {fieldErrors.complainant && <p className="field-error">{fieldErrors.complainant}</p>}

      <label>
        Ward (1-15)
        <input name="ward" type="number" min="1" max="15" value={form.ward} onChange={handleChange} />
      </label>
      {fieldErrors.ward && <p className="field-error">{fieldErrors.ward}</p>}

      <label>
        Category
        <select name="category" value={form.category} onChange={handleChange}>
          <option value="">Select a category</option>
          {categories.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </label>
      {fieldErrors.category && <p className="field-error">{fieldErrors.category}</p>}

      <label>
        Description
        <textarea name="description" rows="3" value={form.description} onChange={handleChange} />
      </label>
      {fieldErrors.description && <p className="field-error">{fieldErrors.description}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Saving...' : 'Record Grievance'}
      </button>
    </form>
  )
}
