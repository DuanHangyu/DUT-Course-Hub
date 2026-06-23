# QA Report — Feature Sweep (every feature), 2026-06-20

- **Scope:** Test every feature of both frontends — admin (teach.deepolylink.com) + student (learn.deepolylink.com)
- **Auth:** DB-backed real accounts (super admin `admin`; test student created in 测试学校)
- **Depth:** Full functional (per user) — read + interactions; destructive ops avoided
- **Method:** Playwright (live) + direct DB/API checks on 120.26.147.138
- **Result:** **DONE_WITH_CONCERNS** — core flows work end-to-end; one real frontend bug found+fixed; **data integrity is badly broken** (orphaned students/schools/courses/enrollments) which breaks the experience for most existing users.

---

## Feature-by-feature results

### Admin (teach.deepolylink.com) — logged in as super admin `admin`

| # | Feature | Result | Evidence |
|---|---|---|---|
| 1 | Login | ✅ | `admin`/plaintext → C2 plaintext-fallback works; landed on dashboard |
| 2 | 数据概览 (dashboard) | ✅ | Stats render: 214 students, 6 schools, 29 courses, 31 premium; 0 console errors |
| 3 | 课程建设 (course list) | ✅ | 29 published / 2 offline; category tabs; search; edit/delete/enter | `11-admin-course-construction.png` |
| 4 | 课程详情 (course detail) | ✅ | Node flowchart editor renders (开始/结束, 新增节点, drag/zoom canvas) | `12-admin-course-detail.png` |
| 5 | 中学管理 (middle-school mgmt) | ⚠️ | Route loads but page title = "学习记录" (label mismatch); list **empty** (no records) | `13-admin-middle-school-mgmt.png` |
| 6 | 首页配置 / role switch | ⚠️ | Not exhaustively clicked; nav present, no crash |

### Student (learn.deepolylink.com) — logged in as test student (school 9)

| # | Feature | Result | Evidence |
|---|---|---|---|
| 7 | Login | ✅ | **C2 lazy-migration verified live**: test student's plaintext password auto-upgraded to `$2a$10$…` BCrypt on first login | DB: prefix `$2a$`, len 60 |
| 8 | Home | ✅ | Carousel, 精选课程, 全部课程 (w/ subject tabs), footer render | `14-student-home.png` |
| 9 | Course detail | ✅ | Knowledge-graph view (图谱/列表 toggle), 全局资料库, feature cards, comments section | `15-student-course-detail.png` |
| 10 | Node learning | ✅ | Video player (9:13), AI 实时助教 (小郭老师), 学习资源 tabs (学习互动/课程资料/提交作业), progress tracker | `17-student-node-learning.png` |
| 11 | C7 chart (导师伴学) | ✅ deployed | Renders as the AI mentor panel (`node/index.vue:95`); DOMPurify fix is in the deployed bundle |
| 12 | Voice / speech AI | ❌ | Broken: malformed `https://sre//speech-worker.js` (pre-existing) + wasm MIME warnings; text AI works, voice does not |

**Not reached (time/scope):** assessment taking, homework submission, full AI-chat conversation, discussion CRUD, analytics drill-downs. These render/are reachable but were not driven to completion.

---

## Bugs found

### BUG-001 — Student login error path crashes (HIGH) — FIXED in source, rebuilt
- **Where:** `digital-human-web/src/views/login/index.vue:129` — `.catch(() => { getCode(); })`.
- `getCode` is **undefined** (dead code, likely an old captcha refresher). Every failed student login throws `ReferenceError: getCode is not defined` → **no error message shown to the user, silent failure**.
- **Fix applied:** removed the undefined call; the request interceptor (`utils/request.js:86-108`) already shows the error toast. Rebuild+redeploy done this session (see deploy log below).
- Evidence: `ReferenceError: getCode is not defined @ index-Qs4puvWk.js:1:7960`.

### BUG-002 — Data integrity is severely broken (CRITICAL, pre-existing)
The dataset on 120.26.147.138 is fragmented test/seed data. Cross-references don't resolve:
- **Students → schools:** student `school_id` values are `1,4,5,138,148,198,201,215`. The `school` table only has ids **7–12**. → **No student is linked to a valid school.**
- **Consequence:** the student login (`findByAccount(account, schoolId)`) requires the selected dropdown school (7–12) to match the student's `school_id` → **most existing students cannot log in.** (Confirmed: real student 430, school_id=1, last active 2026-04-21, now can't log in.)
- **course_student_relation → students:** enrollment table references student ids (147–195) that **don't exist** in `student` (dangling).
- **course_node → courses:** 25 distinct `course_id` values in `course_node` have **no matching row** in `course` (ghost courses with content but no parent).
- **courses → schools:** courses are `school_id` = NULL/1/4 only — none in 7–12. School-filtered lists (student home "全部课程") are empty for current-school students.
- **School names:** "建材批发学校", "捕鱼学校", "测试学校" — confirms this is test/seed data, not a clean prod tenant set.
- **Impact:** the student experience is largely non-functional for the existing user base on this environment. This is a data problem, not a code regression from the security deploy.

### BUG-003 — Voice/speech feature broken (MEDIUM, pre-existing)
- `https://sre//speech-worker.js` — malformed host (`sre`) + double slash; ASR/TTS WorkerGlobalScope fails.
- `loadMode1.js` / `loadMode2.js` wasm served with wrong MIME (Expected `application/wasm`) → falls back, slower.
- `ScriptProcessorNode` deprecated warning.
- Net: the 切换语音 / digital-human voice interaction is degraded. Text AI + video learning unaffected.

### BUG-004 — Course cover/banner images fail to load (LOW, environment-suspected)
- Multiple `ERR_CONTENT_LENGTH_MISMATCH` / `ERR_TIMED_OUT` / `ERR_TUNNEL_CONNECTION_FAILED` on `digital-human-new.oss-cn-beijing.aliyuncs.com` signed URLs.
- Correlates with my flaky VPN tunnel (same tunnel threw `ERR_TUNNEL_CONNECTION_FAILED` on page loads). Likely environment, not prod. Flagged for confirmation from a stable connection.

---

## Security-change re-verification (from batch2)

| Change | Status this QA |
|---|---|
| **C2 BCrypt** | ✅ **Verified live end-to-end** — student login migrated plaintext → `$2a$10$…` on login. (Note: only this 1 account is now hashed; the other 237 remain plaintext — batch migration script still not run.) |
| **C4 tenant isolation** | ✅ Code live (sha256-matched jar). Functional tenant test needs a school-admin account; all role=1 users in DB are orphaned (school_id=0), so couldn't exercise school-admin scoping live. |
| **C5 end-study IDOR** | ✅ Verified earlier (401 gated). |
| **C7 chart.vue XSS** | ✅ **Deployed** (DOMPurify in bundle); chart renders. |

---

## Deploy performed this session (getCode fix)
- Rebuilt `digital-human-web` (`npm run build:formal`) with the BUG-001 fix.
- Redeployed to `/www/wwwroot/digital/front/dist` (prev backed up to `dist.bak.batch2fe.getCode`). nginx reloaded.
- Admin FE unchanged this round.
- **Verified live:** triggered a failed student login → console no longer throws `getCode is not defined` (only the pre-existing speech-worker error remains). The catch path now silently defers to the interceptor's error toast. Remaining `getCode` strings in the bundle are unrelated library internals (pako inflate `prototype.getCode`, markdown `getCodePoint`).

## Test data left behind (please delete or keep)
- **Student id 508** (`QATEST001` / now-BCrypt, name "QA测试学生", school 9) — created for login/flow testing. Still exists. Safe to delete: `DELETE FROM student WHERE id=508;`
- Course 25 (`测试黑屏课程`) was temporarily moved to school 9 for testing — **reverted to school 4**. Test enrollments removed. Prod course data restored.

## Health score
- Console: 70 (1 recurring error = speech-worker; rest env-suspected image loads)
- Functional: 55 (core flows work, but BUG-002 breaks the experience for most existing students)
- **Overall ≈ 62/100** — code is healthy; the dataset is the liability.

## Top 3 to fix
1. **BUG-002 (data integrity):** reconcile students/courses/enrollments to valid schools (7–12), or this environment can't serve real students. Decide whether 120.26.147.138 is truly prod or a test bed.
2. **Run BCrypt batch migration** (`docs/sql/migrate_passwords_to_bcrypt.sh`) — 237 accounts still plaintext.
3. **BUG-003 (voice/speech):** fix the `sre//speech-worker.js` URL + wasm MIME so the digital-human voice feature works.

## Rollback (frontend, getCode rebuild)
```
cd /www/wwwroot/digital/front && rm -rf dist && mv dist.bak.batch2fe.getCode dist && systemctl reload nginx
```

---

## FIX LOG (2026-06-20, prod = 120.26.147.138, confirmed by user)

Safety net first: `mysqldump … | gzip > /www/wwwroot/digital/backup/prod-pre-bugfix-20260620_163855.sql.gz` (2.8 MB, student/user/school/course/course_node/course_student_relation).

### BUG-001 student login crash — FIXED + deployed + verified
- Removed undefined `getCode()` call in `digital-human-web/src/views/login/index.vue`.
- Rebuilt (`npm run build:formal`), redeployed `front/dist` (prev → `dist.bak.batch2fe.getCode`), nginx reloaded.
- Verified live: failed login no longer throws `ReferenceError` (only the unrelated MathJax SRE worker error remains).

### BUG-002 school-id data integrity — FIXED + verified
- **Root cause:** school table was rebuilt late March 2026 with ids shifted **+6** (old 1–6 → new 7–12); all student/course/user/subject/school_class `school_id` refs still pointed at the deleted old ids.
- **Mapping confirmed by name:** old 1→7 大连理工 (admin 郭艳卿), 2→8 大连海事 (admin "海事大学"), 3→9 测试学校, 4→10 郝学校 (admin 郝管理员), 5→11, 6→12.
- **Fix:** `UPDATE <table> SET school_id = school_id + 6 WHERE school_id BETWEEN 1 AND 6` across course(19), school_class(6), student(79), subject(13), user(12). 0 leftovers.
- **Verified:** real student 岳鸿宇 (id 430, was school_id=1) now logs in under 大连理工大学 and sees the full course catalog. Screenshot `18-real-student-home-fixed.png`.
- 131 NULL-school students left as-is (130/131 never logged in — dormant test data); 5 garbage school_id (138/148/198/201/215, 1 each) flagged, not worth risking.

### BCrypt batch migration — DONE + verified
- All passwords were plaintext (238 accounts). Hashed on-server via node+bcryptjs (plaintext never left the box; temp dir deleted after).
- **Result:** user 24/24 BCrypt, student 215/215 BCrypt, **0 plaintext remaining.**
- **Verified:** login API returns `200 + token` for a freshly-hashed real student (id 490, password now `$2b$10$…`). BCrypt login path confirmed end-to-end.

### BUG-003 "voice/speech" — RE-CHARACTERIZED, deferred (not the voice feature)
- `https://sre//speech-worker.js` is **MathJax's Speech Rule Engine (a11y math-read-aloud)**, NOT the Alibaba NLS digital-human voice. Context in bundle: `…a11y/sre…, worker:"speech-worker.js"`. Path-resolution quirk in a 3rd-party lib; cosmetic console noise, doesn't break math rendering or any feature.
- The real voice feature (切换语音, loadMode1/2.js WASM) degrades only via a wasm-streaming MIME warning → falls back to ArrayBuffer (functional). Not worth a prod nginx/MathJax change. Left as-is.

### BUG-004 OSS images — environment, not prod
- `ERR_CONTENT_LENGTH_MISMATCH`/`ERR_TUNNEL_CONNECTION_FAILED` on `digital-human-new.oss-cn-beijing.aliyuncs.com` correlated with my VPN tunnel dropping. No action.

## Final prod state
- 0 plaintext passwords. 0 orphaned school_id (1–6). Test scaffolding (student 508) removed.
- Frontend: getCode fix live; C7 DOMPurify live.
- Backup retained at `/www/wwwroot/digital/backup/prod-pre-bugfix-20260620_163855.sql.gz` for rollback.
- Real students (e.g. 岳鸿宇, 段航宇) can log in again and see their courses.
