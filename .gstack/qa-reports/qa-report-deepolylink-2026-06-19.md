# QA Report — DUT-Research Course Hub (post batch2 security deploy)

- **Date:** 2026-06-19
- **Tier:** Standard (report-only — production; no in-place fix loop)
- **Target:** Backend `120.26.147.138:8083` (digital-human prod) + frontends teach/learn.deepolylink.com
- **Scope:** Verify batch2 security changes (C2 BCrypt, C4 tenant isolation, C5 end-study IDOR, C7 XSS) are live and functional
- **Method:** Playwright (live sites) + direct backend/API/DB checks on the server
- **Health score:** Backend **90/100** · End-to-end (FE→BE) **45/100** (frontend config broken, pre-existing)

---

## Headline

The **backend security deploy is live and healthy** — new code is running, login endpoint responds correctly, the IDOR fix is verified at the endpoint level, and the service is UP. But **two issues need your attention**, neither caused by this deploy:

1. **BCrypt migration hasn't touched any existing password (0/238 hashed).** Login still works via the plaintext fallback, but every account is still recoverable in cleartext until each one logs in.
2. **The deployed frontends point at `*.hotsupper.top` TEST backend domains (invalid SSL), not the prod backend.** The student site can't even load its school dropdown. This is a pre-existing frontend build-config problem, and the C7 XSS fix (frontend) was never rebuilt/deployed.

---

## Per-change verification

### C2 — Password BCrypt ⚠️ CODE LIVE, DATA NOT MIGRATED
- ✅ `PasswordUtils.class` present in deployed jar (sha256-verified build).
- ✅ Login endpoint healthy: `POST /login` returns proper `{code,message,data}` envelope, code 203 for unknown account, no 500s. Plaintext-fallback + lazy-migration path won't break existing logins.
- ⚠️ **DB state (queried directly):** user table **0/24** BCrypt, student table **0/214** BCrypt. **All 238 passwords are still plaintext.** Lazy migration only hashes an account on its next successful login; inactive accounts are never covered. A DB dump today exposes every password in cleartext.
- **Recommendation:** run `docs/sql/migrate_passwords_to_bcrypt.sh` (created earlier, never executed), OR explicitly accept lazy migration and document that inactive accounts stay plaintext.

### C4 — Tenant isolation ✅ CODE LIVE (functional test needs an admin session)
- ✅ DB-backed role/schoolId checks in UserAppService / StudentAppService / TeacherAppService / SchoolAppService / StudyMonitor (2 rounds of codex review, clean compile, sha256-matched jar).
- ⚠️ **Data-quality note (pre-existing):** some students have `school_id = NULL`; some users have `school_id = 0`. With the new fail-closed checks, school admins correctly can't see NULL-school students — but those records are effectively orphaned. Not caused by this deploy.
- Could not run an authenticated end-to-end test — no admin/teacher test credentials provided.

### C5 — end-study IDOR ✅ VERIFIED LIVE
- ✅ `POST /api/course/end-study` now returns `{"code":401,"message":"未能读取到有效 token"}` — requires auth. Before the fix this route was in Sa-Token's anonymous whitelist (the IDOR hole). The gate is now closed.
- ✅ Code (uses `StpUtil.getLoginIdAsInt()`, ignores client-supplied `studentId`) is in the sha256-matched jar.

### C7 — chart.vue XSS ❌ NOT DEPLOYED
- ❌ Only the backend jar was deployed. The DOMPurify fix in `digital-human-web/src/views/node/components/chart.vue` is in the source but the frontend dist was **never rebuilt/redeployed**. The live student frontend still runs the old, vulnerable chart component.
- Action: rebuild + redeploy the student frontend (see frontend issues below — rebuild must use the correct env first).

---

## Frontend topology issues (pre-existing, blocking end-to-end QA)

### ISSUE-001 — Student FE calls a TEST backend domain (HIGH)
- `digital-human-web/.env.production` bakes in `https://digital-student-test.hotsupper.top/digital/`. This is the default Vite build mode, so the deployed student dist calls a **test domain with an invalid SSL cert** (`ERR_CERT_AUTHORITY_INVALID`, `ssl_verify=18`).
- Observed live: `learn.deepolylink.com/login` throws a network error fetching `/digital/allSchool` → **school dropdown is empty → students cannot log in.**
- The **correct** prod URLs already exist in `digital-human-web/.env.formal` (`https://learn.deepolylink.com/digital`), but that build mode (`--mode formal`) wasn't used for the deployed dist.
- **Fix:** rebuild student FE with `--mode formal` (or fix `.env.production` to point at `learn.deepolylink.com/digital`), then redeploy `dist` to the server's student-frontend root.

### ISSUE-002 — Admin FE also points at a TEST backend domain (HIGH)
- `digital-human-admin/.env.production` → `https://digital-test.hotsupper.top/digital/`. Login page loads cleanly only because it makes no API call on load; an actual login POST would hit the same invalid-cert test domain.
- **Fix:** same pattern — point admin FE at the prod backend, rebuild, redeploy.

### ISSUE-003 — 120.26.147.138 nginx does not serve teach/learn.deepolylink.com (NEEDS CLARIFICATION)
- This server's nginx vhosts are only: `defense.deepolylink.com`, `yan-mian-ya.deepolylink.com`, `qrcode.yan-mian-ya.deepolylink.com`. There is **no** vhost for `teach` or `learn`.deepolylink.com, and these domains don't resolve via the server's public DNS.
- So the backend I deployed (8083) has **no public-domain route** on this box. Where real users actually reach it is not determinable from this environment (my local DNS is VPN-intercepted to `198.18.x.x`).
- **Question for you:** is `120.26.147.138` really the box students/admins hit? If the deepolylink frontends are served + proxied by a *different* host (CDN/another server), I deployed to the right code but need that host's nginx to route `/digital/*` → `120.26.147.138:8083` (or the FE env to point here).

### ISSUE-004 — Malformed speech-worker URL (LOW)
- Student FE also errors on `https://sre//speech-worker.js` (bad host + double slash) — the ASR/TTS worker config. Pre-existing, secondary to ISSUE-001.

---

## Console health
- Admin FE (`teach.deepolylink.com/login`): **0 errors, 0 warnings.** Clean.
- Student FE (`learn.deepolylink.com/login`): **3 errors** — all from ISSUE-001/004 (failed `allSchool` fetch + speech worker). None from the backend.

## Backend health
- `actuator/health` → `UP`. Started in 28.2s, profile=prod, port 8083, PID 754360.
- `/allSchool` → 200, 6 schools. `/login` → proper envelope. `/api/course/end-study` → 401 (gated).
- Neighbor services untouched: intelligent-agent (8086), yan-mian-ya (8081) still running.

---

## What I could NOT verify (blocked, needs you)
- **Authenticated flows** (C4 tenant isolation end-to-end, C5 end-study happy-path, BCrypt lazy-migration on a real login): I have no valid admin/student credentials and did not exfiltrate passwords from the DB. Provide a throwaway test account to close this gap.
- **True production topology** (does 120.26.147.138 serve the deepolylink domains?): opaque from this network.

## Screenshots
- `screenshots/01-admin-login.png` — admin login, clean
- `screenshots/02-student-login.png` — student login, broken school dropdown

## Ship readiness
- Backend security fixes: **shipped & verified** (C2 code/C5/C4 code live; C2 data migration pending decision).
- Frontend: **not shippable as-is** — needs env fix + rebuild + redeploy (ISSUE-001/002), which also delivers the missing C7 XSS fix.

---

## UPDATE 2026-06-19 22:45 — Frontend rebuilt & redeployed ✅

Topology resolved: `/www/wwwroot/nginx/conf/learn-deepolylink.conf` serves **both** domains on this server —
- `learn.deepolylink.com` (student) → root `/www/wwwroot/digital/front/dist`, `/digital/` → `127.0.0.1:8083`
- `teach.deepolylink.com` (admin) → root `/www/wwwroot/digital/backend/dist`, `/digital/` → `127.0.0.1:8083`

So my backend deploy IS the prod backend for both. The nginx confs live in `/www/wwwroot/nginx/conf/` (not the BT-panel vhost dir — that's why they were missed initially).

**Action taken:** rebuilt both FEs with the correct env (`npm run build:formal` → `.env.formal`), redeployed:
- Student dist → `/www/wwwroot/digital/front/dist` (backed up to `dist.bak.batch2fe`)
- Admin dist → `/www/wwwroot/digital/backend/dist` (backed up to `dist.bak.batch2fe`)
- Ownership `www:www`, nginx reloaded (config valid).

**Verification (Playwright, post-deploy):**
| Surface | Before | After |
|---|---|---|
| Student `learn.deepolylink.com/digital/allSchool` | ERR_CERT_AUTHORITY_INVALID (test domain) | **200, 6 schools** ✅ |
| Student console errors | 3 (cert + speech-worker) | **1** (only pre-existing speech-worker URL) |
| Admin `teach.deepolylink.com/digital/allSchool` | n/a (login page made no call) | **200, 6 schools** ✅ |
| Admin console errors | 0 | **0** ✅ |
| Built JS references to `*.hotsupper.top` | present in both dists | **0 in both** ✅ |
| C7 DOMPurify in student bundle | not deployed | **bundled** ✅ |

**C7 (chart.vue XSS): now PUBLISHED** — DOMPurify is in the deployed student bundle.

**ISSUE-001, ISSUE-002, C7: RESOLVED.** ISSUE-003 (topology) resolved — both domains confirmed on this server. ISSUE-004 (speech-worker `sre//` URL) remains — pre-existing, voice/digital-human feature only, does not block login or core learning; deferred.

**Still open (unchanged from above):**
- C2 data: 0/238 passwords BCrypt-hashed (lazy migration only; batch script not run).
- Authenticated-flow verification still needs a test account.

**Rollback (frontend):**
```
cd /www/wwwroot/digital/front && rm -rf dist && mv dist.bak.batch2fe dist
cd /www/wwwroot/digital/backend && rm -rf dist && mv dist.bak.batch2fe dist
systemctl reload nginx
```

**New screenshots:** `03-student-login-after.png`, `04-admin-login-after.png`.
