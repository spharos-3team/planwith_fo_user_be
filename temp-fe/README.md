# PlanWith 임시 FE (로컬 클릭 테스트)

Gateway(`:8000`) + fo-user-be(`:8080`)에 붙는 **임시** 프론트입니다. 팀 FE가 오면 삭제해도 됩니다.

## 실행

1. Discovery / fo-user-be / Gateway / MySQL이 떠 있는지 확인
2. 이 폴더에서:

```bash
npm install
npm run dev
```

3. 브라우저: http://localhost:5173

Vite가 `/api` → Gateway, `/files` → BE로 프록시합니다 (쿠키·CORS 이슈 회피).

## 추천 클릭 시나리오

1. 홈 → 시작하기 → 약관 동의 → 이메일 인증 → 가입
2. 내 프로필에서 UUID 복사 → 프로필 수정
3. 시크릿 창에서 두 번째 계정 가입 → UUID로 첫 계정 열기 → 팔로우
4. 상단 **등급** 메뉴 → 내 등급/카탈로그 확인 → 평가(story/like) → 월간 보상
5. 로그아웃 / 재로그인
