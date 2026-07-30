#!/bin/bash
# PlanWith FO User Backend 회원가입~로그아웃 전체 플로우 curl 테스트 스크립트.
# 서버가 로컬 8080 포트에 떠 있고, email.verification.mock-mode=true (기본값) 상태를 가정합니다.
#
# 사용법:
#   1) 서버를 먼저 실행한다 (./gradlew bootRun)
#   2) 이 스크립트를 실행: bash testing/test-flow.sh
#   3) 4번 단계 실행 후 "이메일 인증코드 입력" 프롬프트가 뜨면,
#      서버를 실행 중인 터미널 콘솔에 찍힌 "[MOCK MAIL] ... 인증코드: 123456" 로그를 보고 그 6자리 숫자를 입력한다.

BASE_URL="http://localhost:8080"
EMAIL="test$(date +%s)@example.com"   # 매번 다른 이메일로 (중복 방지)
PASSWORD="Test1234!"
NICKNAME="여행자$RANDOM"

echo "===================================================="
echo "1) 이메일 중복확인 (false 여야 정상 - 새 이메일이라)"
echo "===================================================="
curl -s "$BASE_URL/api/auth/check-email?email=$EMAIL" | python3 -m json.tool

echo
echo "===================================================="
echo "2) 약관 목록 조회"
echo "===================================================="
curl -s "$BASE_URL/api/terms" | python3 -m json.tool

echo
echo "===================================================="
echo "3) 이메일 인증코드 발송"
echo "===================================================="
curl -s -X POST "$BASE_URL/api/auth/email/send" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\"}" | python3 -m json.tool

echo
echo "서버 콘솔에서 [MOCK MAIL] 로그를 확인하세요. 예: [MOCK MAIL] $EMAIL 로 발송할 인증코드: 483920"
read -p "인증코드 6자리를 입력하세요: " CODE

echo
echo "===================================================="
echo "4) 이메일 인증코드 확인"
echo "===================================================="
curl -s -X POST "$BASE_URL/api/auth/email/verify" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"code\":\"$CODE\"}" | python3 -m json.tool

echo
echo "===================================================="
echo "5) 회원가입 (약관 id 1,2,3은 schema.sql에 시드된 필수 약관)"
echo "===================================================="
curl -s -X POST "$BASE_URL/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\",
    \"nickname\": \"$NICKNAME\",
    \"introduction\": \"안녕하세요, 테스트 계정입니다.\",
    \"agreedTermIds\": [1, 2, 3]
  }" | python3 -m json.tool

echo
echo "===================================================="
echo "6) 로그인"
echo "===================================================="
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "$LOGIN_RESPONSE" | python3 -m json.tool

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['refreshToken'])")

echo
echo "===================================================="
echo "7) 토큰 재발급"
echo "===================================================="
curl -s -X POST "$BASE_URL/api/auth/reissue" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | python3 -m json.tool

echo
echo "===================================================="
echo "8) 로그아웃 (인증 필요 - Authorization 헤더 확인)"
echo "===================================================="
curl -s -X POST "$BASE_URL/api/auth/logout" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | python3 -m json.tool

echo
echo "테스트 완료. 위 각 단계에서 \"success\": true 가 나왔으면 정상 동작하는 겁니다."
