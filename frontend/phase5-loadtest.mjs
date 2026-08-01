// Phase 5 — Re-test the Phase 1 hospital-registration lock/transaction fix under load.
// Fires N concurrent POST /api/v1/hospitals/register (the atomic tenant+hospital+admin
// transaction the fix targets). Baseline (before fix): ~59s + Lock wait timeout (HTTP 500).
// Expect: all requests HTTP 201, fast wall time, all rows committed in MySQL.
const N = Number(process.argv[2] ?? 5);
const BASE = 'http://localhost:8080/api/v1/hospitals/register';

function payload(i) {
  const ts = Date.now();
  return {
    hospitalName: `LoadTest ${i} ${ts}`,
    hospitalEmail: `lt-${i}-${ts}@hms.test`,
    hospitalPhone: '+1-555-0100',
    hospitalAddress: `${i} Test Drive`,
    subscriptionPlan: 'STANDARD',
    adminFirstName: `Load${i}`,
    adminLastName: 'Test',
    adminEmail: `lt-admin-${i}-${ts}@hms.test`,
    adminPassword: 'LoadTest!Passw0rd1',
    adminPhone: '+1-555-0199',
  };
}

const results = await Promise.all(
  Array.from({ length: N }, async (_, i) => {
    const start = performance.now();
    try {
      const res = await fetch(BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload(i)),
      });
      const ms = Math.round(performance.now() - start);
      return { i, status: res.status, ms, ok: res.ok };
    } catch (e) {
      return {
        i,
        status: 'ERR',
        ms: Math.round(performance.now() - start),
        ok: false,
        error: e.message,
      };
    }
  }),
);

const wallStart = Date.now();
// wall time already captured implicitly; recompute simple aggregate
const totalMs = Math.max(...results.map((r) => r.ms));
const okCount = results.filter((r) => r.ok).length;
const errs = results.filter((r) => !r.ok);

console.log(`LOADTEST N=${N}`);
console.log('per-request:');
for (const r of results.sort((a, b) => a.i - b.i)) {
  console.log(`  #${r.i} -> HTTP ${r.status} in ${r.ms}ms${r.error ? '  ' + r.error : ''}`);
}
console.log(`SUMMARY: ok=${okCount}/${N}  slowest=${totalMs}ms  failures=${errs.length}`);
console.log(`PASS=${okCount === N && errs.length === 0}`);
