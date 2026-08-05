# PlanWith FO User BE — Frontend API 인수인계

Base URL (로컬 MSA): `http://localhost:8000` (Gateway)  
BE 직접 호출은 하지 마세요. Gateway가 JWT 검증 및 내부 trust 헤더를 주입합니다.

공통 응답:

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-08-04T07:00:00Z"
}
```

실패 시:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "사람이 읽을 메시지",
    "fieldErrors": [{ "field": "email", "message": "..." }]
  },
  "timestamp": "..."
}
```

FE 필수:

- `credentials: "include"` (Refresh Cookie)
- 인증 API는 `Authorization: Bearer <accessToken>` (Gateway가 보호하는 엔드포인트)
- CORS 허용 origin 예: `http://localhost:3000`, `http://localhost:5173`

---

## 1. 회원가입 플로우 (로컬 이메일)

```
GET  /api/v1/terms
GET  /api/v1/terms/docs/{slug}          # service | privacy | age | marketing
GET  /api/v1/auth/check-email?email=
GET  /api/v1/auth/check-nickname?nickname=
POST /api/v1/auth/email/send            { "email" }
POST /api/v1/auth/email/verify          { "email", "code" }
POST /api/v1/auth/profile-image         multipart file (optional, before signup)
POST /api/v1/auth/signup
POST /api/v1/auth/login
```

### 약관

`GET /api/v1/terms`

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "이용약관 동의",
      "contentUrl": "/api/v1/terms/docs/service",
      "required": true
    }
  ]
}
```

- 필수 약관(`required: true`) id는 signup / social-signup의 `agreedTermIds`에 모두 포함
- 본문 HTML: Gateway 기준으로 `contentUrl`을 그대로 열면 됨  
  예) `http://localhost:8000/api/v1/terms/docs/service`

### 이메일/닉네임 중복

- `data: true` → **이미 사용 중(중복)**
- `data: false` → 사용 가능

### 이메일 인증

```http
POST /api/v1/auth/email/send
{ "email": "user@example.com" }

POST /api/v1/auth/email/verify
{ "email": "user@example.com", "code": "123456" }
```

로컬 mock 메일일 때만 (운영 불가):

```http
GET /api/v1/dev/email-code?email=user@example.com
```

### 프로필 이미지 (가입 전 업로드)

```http
POST /api/v1/auth/profile-image
Content-Type: multipart/form-data
file: <image>
```

제약:

- 형식: jpg / jpeg / png / webp
- 크기: 최대 5MB
- 픽셀: **400x400 정사각형**
- 성공 시 `data` = 이미지 URL 문자열 → signup의 `profileImage`에 넣기

### 회원가입

```http
POST /api/v1/auth/signup
{
  "email": "user@example.com",
  "password": "Passw0rd!",
  "nickname": "닉네임",
  "profileImage": "/files/....jpg",
  "introduction": "소개(최대20자)",
  "agreedTermIds": [1, 2, 3]
}
```

비밀번호: 영문 + 특수문자 포함 8~20자  
닉네임: 2~10자 (비속어 필터 적용)

---

## 2. 로그인 / 토큰 / 로그아웃

### 로그인

```http
POST /api/v1/auth/login
{ "email": "...", "password": "..." }
```

응답 `data`:

```json
{
  "tokenType": "Bearer",
  "accessToken": "<JWT>",
  "accessTokenExpiresIn": 900,
  "user": {
    "userId": "1",
    "roles": ["USER"],
    "scopes": ["profile:read", "plan:read"]
  }
}
```

- **Refresh Token은 body에 없음**
- `Set-Cookie: refresh_token=...; HttpOnly; Path=/api/v1/auth; SameSite=Lax`
- Access Token은 메모리/상태에만 보관 권장 (localStorage 비권장)

### Access 갱신

```http
POST /api/v1/auth/refresh
```

Cookie의 `refresh_token` 자동 전송. 새 accessToken + refresh cookie 회전.

### 로그아웃

```http
POST /api/v1/auth/logout
```

Cookie 기반. refresh 폐기 + cookie clear.

### 전체 세션 로그아웃 (인증 필요)

```http
POST /api/v1/auth/logout-all
Authorization: Bearer <accessToken>
```

### 탈퇴 (인증 필요, soft delete)

```http
DELETE /api/v1/auth/withdraw
Authorization: Bearer <accessToken>
{ "password": "..." }
```

- DB row는 **삭제하지 않음**. `member.status`만 `DELETED`로 바꾸고 `deleted_at` 설정 + 이메일/닉네임 등 개인정보 익명화
- 회원 상태: `ACTIVE` | `SUSPENDED` | `DELETED`  
  - 로그인/프로필/팔로우는 `ACTIVE`만 가능  
  - `SUSPENDED`는 정지, `DELETED`는 탈퇴
- 로컬 가입: `password` 필수 (본인 확인)
- 소셜 가입: `password` 생략 가능
- 성공 시 refresh 세션 전부 폐기. FE는 accessToken도 폐기하고 로그인 화면으로

### 비밀번호 재설정

이메일 인증 코드 발송(`email/send`) 후:

```http
POST /api/v1/auth/password/reset
{
  "email": "...",
  "code": "123456",
  "newPassword": "Passw0rd!"
}
```

---

## 3. 소셜 로그인 (Google / Kakao / Naver)

Provider 값: `GOOGLE` | `KAKAO` | `NAVER` (대소문자 무시, 권장 대문자)

### A) 클라이언트가 accessToken을 이미 가진 경우

```http
POST /api/v1/auth/social-login
{
  "provider": "KAKAO",
  "accessToken": "<provider access token>"
}
```

### B) authorization code를 BE에서 교환

```http
POST /api/v1/auth/social-login
{
  "provider": "GOOGLE",
  "authorizationCode": "...",
  "redirectUri": "https://app.example.com/oauth/callback",
  "state": "naver일때권장"
}
```

`accessToken` **또는** (`authorizationCode` + `redirectUri`) 중 하나 필수.

### 응답

이미 가입된 계정:

```json
{
  "needsSignup": false,
  "tokens": { "tokenType": "Bearer", "accessToken": "...", "accessTokenExpiresIn": 900, "user": {} }
}
```

미가입:

```json
{
  "needsSignup": true,
  "tokens": null,
  "provider": "KAKAO",
  "email": "optional@...",
  "suggestedNickname": "..."
}
```

→ 약관 동의 후 social-signup:

```http
POST /api/v1/auth/social-signup
{
  "provider": "KAKAO",
  "accessToken": "...",
  "nickname": "선택닉네임",
  "agreedTermIds": [1, 2, 3]
}
```

code 방식도 login과 동일하게 `authorizationCode`/`redirectUri`/`state` 사용 가능.

---

## 4. Gateway에서 public인 경로 (로그인 전)

대략 다음 경로는 JWT 없이 호출 가능합니다.

- `/api/v1/terms`, `/api/v1/terms/**`
- `/api/v1/auth/signup`, `/login`, `/refresh`, `/logout`
- `/api/v1/auth/email/**`, `/check-email`, `/check-nickname`
- `/api/v1/auth/social-login`, `/social-signup`
- `/api/v1/auth/password/reset`, `/profile-image`
- `/oauth2/jwks`
- 로컬 전용 `/api/v1/dev/**`

그 외(예: `logout-all`, `withdraw`, SSE ticket, `/api/v1/members/**` 중 인증 API 등)는 Bearer 필요.  
`GET /api/v1/grades`, `/api/v1/internal/**` 은 Gateway public (내부 API는 trust 헤더로 보호).

---

## 5. 프로필 / 팔로우 (인증 필요)

모두 `Authorization: Bearer <accessToken>` 필요 (Gateway 기본 보호).

### 내 프로필 조회

```http
GET /api/v1/members/me
```

응답 `data` 예:

```json
{
  "memberId": 1,
  "memberUuid": "a1b2c3d4-...",
  "nickname": "닉네임",
  "profileImage": "/files/....jpg",
  "profileIntro": "소개",
  "grade": "ROOKIE",
  "email": "user@example.com",
  "followerCount": 12,
  "followingCount": 3,
  "followedByMe": null
}
```

### 내 프로필 수정

```http
PATCH /api/v1/members/me
{
  "nickname": "새닉네임",
  "profileImage": "/files/....jpg",
  "profileIntro": "새소개"
}
```

- 보낸 필드만 반영 (null/미포함은 유지). `profileImage`/`profileIntro`를 `""`로 보내면 비움.
- 닉네임: 2~10자, 비속어·중복 검사
- 소개: 최대 20자

### 다른 회원 프로필

```http
GET /api/v1/members/{memberUuid}
```

- `email` / `memberId`는 노출하지 않음
- `followedByMe`: 내가 그 회원을 팔로우 중이면 `true`/`false`

### 팔로우 / 언팔로우

```http
POST   /api/v1/members/{memberUuid}/follow
DELETE /api/v1/members/{memberUuid}/follow
```

에러 코드: `CANNOT_FOLLOW_SELF`, `ALREADY_FOLLOWING`, `NOT_FOLLOWING`, `USER_NOT_FOUND`

### 팔로워 / 팔로잉 목록

```http
GET /api/v1/members/{memberUuid}/followers
GET /api/v1/members/{memberUuid}/following
```

`data`는 프로필 요약 배열 (`memberUuid`, `nickname`, `profileImage`, `profileIntro`, `grade`, counts).

---

## 6. 회원 등급

프로필 `grade` 필드는 등급 코드입니다: `ROOKIE` | `LEAF` | `TRAVELER` | `EXPLORER` | `ADVENTURER` | `MASTER`  
가입 시 기본값 `ROOKIE`.

### 등급 목록 (공개)

```http
GET /api/v1/grades
```

각 항목: `gradeCode`, `nameKo`, `sortOrder`, `monthlyTokenAmount`, `conditions[]` (`metricType`=`STORY|FOLLOWER|LIKE`, `threshold`), `benefits[]` (`benefitCode`, `description`).

### 내 등급 / 보상 내역 (인증 필요)

```http
GET /api/v1/members/me/grade
GET /api/v1/members/me/grade/rewards
GET /api/v1/members/{memberUuid}/grade
```

`me/grade` 응답에 현재 등급, 지표(`storyCount`/`followerCount`/`likeCount`), 혜택 목록 포함.

### 승급 평가 (내부 연동)

콘텐츠 서비스가 스토리/좋아요 수를 푸시합니다. 팔로워 수는 fo-user-be `follow` 테이블에서 계산합니다.

```http
POST /api/v1/internal/grades/evaluate
{
  "memberUuid": "...",
  "storyCount": 3,
  "likeCount": 30
}
```

- 조건을 모두 충족하는 **최고 등급**으로만 승급 (강등 없음)
- Gateway public + BE gateway trust 헤더 필요

### 월간 토큰 보상 기록 (내부)

지갑 이체는 하지 않고 `grade_reward_history`에만 적재합니다.

```http
POST /api/v1/internal/grades/rewards/monthly
{ "periodYm": "2026-08" }
```

`periodYm` 생략 시 현재 월. 동일 member+period는 skip (idempotent).

---

## 7. 아직 FO User BE에 없는 것 (다른 담당)

- 크리에이터 후원/결제/피드/채팅 API (다른 서비스)
- 토큰 지갑 잔액/차감 (등급 보상 내역만 본 서비스)

---

## 8. 로컬 연동 체크리스트

1. Discovery `:8761`, fo-user-be `:8080`, Gateway `:8000` 기동
2. FE origin을 Gateway CORS에 추가했는지 확인
3. 모든 API는 Gateway(`:8000`)로 호출 + `credentials: "include"`
4. Access Token은 Authorization 헤더로만 사용
5. Refresh는 Cookie path `/api/v1/auth` — refresh/logout도 같은 path prefix로 호출

임시 검증용 `temp-fe`는 삭제되었습니다. 팀 FE를 Gateway에 연결해 주세요.
