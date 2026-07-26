import { useState } from 'react'
import NewGrievanceForm from './components/NewGrievanceForm'
import GrievanceList from './components/GrievanceList'

export default function App() {
  const [refreshKey, setRefreshKey] = useState(0)

  return (
    <div className="app">
      <header>
        <h1>Panchayat Grievance Register &amp; Resolution Tracker</h1>
        <p className="subtitle">Every grievance, tracked until it is closed - oldest unresolved cases first.</p>
      </header>

      <main>
        <NewGrievanceForm onCreated={() => setRefreshKey((k) => k + 1)} />
        <GrievanceList refreshKey={refreshKey} />
      </main>
    </div>
  )
}
