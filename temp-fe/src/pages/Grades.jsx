import { useCallback, useEffect, useState } from "react";
import { api } from "../api";
import { useAuth } from "../auth.jsx";

export default function Grades() {
  const { profile, refreshProfile } = useAuth();
  const [catalog, setCatalog] = useState([]);
  const [myGrade, setMyGrade] = useState(null);
  const [rewards, setRewards] = useState([]);
  const [storyCount, setStoryCount] = useState("3");
  const [likeCount, setLikeCount] = useState("30");
  const [periodYm, setPeriodYm] = useState("");
  const [evalResult, setEvalResult] = useState(null);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loading, setLoading] = useState(false);

  const reload = useCallback(async () => {
    const [grades, grade, rewardList] = await Promise.all([
      api("/api/v1/grades"),
      api("/api/v1/members/me/grade"),
      api("/api/v1/members/me/grade/rewards"),
    ]);
    setCatalog(grades || []);
    setMyGrade(grade);
    setRewards(rewardList || []);
  }, []);

  useEffect(() => {
    reload().catch((err) => setError(err.message));
  }, [reload]);

  return (
    <>
      <section className="section">
        <h1>회원 등급 테스트</h1>
        <p className="hint">
          Gateway 경유 공개/인증 API와 내부 평가·월간 보상까지 클릭으로 확인합니다.
          현재 프로필 grade: <strong>{profile?.grade || "-"}</strong>
        </p>
        <div className="row" style={{ marginTop: 12 }}>
          <button
            className="btn btn-ghost dark"
            type="button"
            disabled={loading}
            onClick={async () => {
              setLoading(true);
              setError("");
              setInfo("");
              try {
                await reload();
                await refreshProfile();
                setInfo("새로고침 완료");
              } catch (err) {
                setError(err.message);
              } finally {
                setLoading(false);
              }
            }}
          >
            새로고침
          </button>
        </div>
      </section>

      {error && <div className="error">{error}</div>}
      {info && <div className="success">{info}</div>}

      {myGrade && (
        <section className="section panel">
          <h2>내 등급</h2>
          <p className="meta">
            {myGrade.gradeCode} · {myGrade.gradeName} (level {myGrade.gradeLevel}) · {myGrade.gradeStatus}
          </p>
          <p className="hint">
            gradeUuid: {myGrade.gradeUuid}
            <br />
            assigned: {myGrade.gradeAssignedAt || "-"} / evaluated: {myGrade.lastEvaluatedAt || "-"}
          </p>
          <h3>메트릭</h3>
          <div className="list">
            {(myGrade.metrics || []).map((m) => (
              <div key={m.metricType} className="list-item">
                <div>
                  <strong>{m.metricType}</strong>
                  <div className="hint">
                    {m.sourceService} · v{m.sourceVersion} · {m.synchronizedAt || "-"}
                  </div>
                </div>
                <span>{m.currentValue}</span>
              </div>
            ))}
          </div>
          <h3 style={{ marginTop: 16 }}>혜택</h3>
          <BenefitList benefits={myGrade.benefits} />
        </section>
      )}

      <section className="section panel">
        <h2>승급 평가 (internal)</h2>
        <p className="hint">
          LEAF 예시: story≥3, follower≥10, like≥30. 팔로워는 follow 테이블에서 계산됩니다.
        </p>
        <div className="field">
          <label>storyCount</label>
          <input value={storyCount} onChange={(e) => setStoryCount(e.target.value)} />
        </div>
        <div className="field">
          <label>likeCount</label>
          <input value={likeCount} onChange={(e) => setLikeCount(e.target.value)} />
        </div>
        <button
          className="btn btn-leaf"
          type="button"
          disabled={loading || !profile?.memberUuid}
          onClick={async () => {
            setLoading(true);
            setError("");
            setInfo("");
            setEvalResult(null);
            try {
              const result = await api("/api/v1/internal/grades/evaluate", {
                method: "POST",
                body: JSON.stringify({
                  memberUuid: profile.memberUuid,
                  storyCount: Number(storyCount),
                  likeCount: Number(likeCount),
                }),
              });
              setEvalResult(result);
              await reload();
              await refreshProfile();
              setInfo(
                result.upgraded
                  ? `승급: ${result.previousGradeCode} → ${result.currentGradeCode}`
                  : `유지: ${result.currentGradeCode}`
              );
            } catch (err) {
              setError(err.message);
            } finally {
              setLoading(false);
            }
          }}
        >
          평가 실행
        </button>
        {evalResult && (
          <pre className="code-block" style={{ marginTop: 12 }}>
            {JSON.stringify(evalResult, null, 2)}
          </pre>
        )}
      </section>

      <section className="section panel">
        <h2>월간 토큰 보상 (internal)</h2>
        <div className="field">
          <label>periodYm (비우면 이번 달)</label>
          <input
            placeholder="2026-08"
            value={periodYm}
            onChange={(e) => setPeriodYm(e.target.value)}
          />
        </div>
        <button
          className="btn btn-leaf"
          type="button"
          disabled={loading}
          onClick={async () => {
            setLoading(true);
            setError("");
            setInfo("");
            try {
              const body = periodYm.trim() ? { periodYm: periodYm.trim() } : {};
              const result = await api("/api/v1/internal/grades/rewards/monthly", {
                method: "POST",
                body: JSON.stringify(body),
              });
              await reload();
              setInfo(`월간 보상 생성 건수: ${result?.createdCount ?? 0}`);
            } catch (err) {
              setError(err.message);
            } finally {
              setLoading(false);
            }
          }}
        >
          월간 보상 지급 기록
        </button>
        <h3 style={{ marginTop: 16 }}>내 보상 내역</h3>
        {!rewards.length ? (
          <p className="hint">보상 내역이 없습니다.</p>
        ) : (
          <div className="list">
            {rewards.map((r) => (
              <div key={`${r.rewardMonth}-${r.createdAt}`} className="list-item">
                <div>
                  <strong>
                    {r.rewardMonth} · {r.gradeCode}
                  </strong>
                  <div className="hint">
                    {r.rewardStatus} · {r.createdAt}
                  </div>
                </div>
                <span>{r.tokenAmount} token</span>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="section">
        <h2>등급 카탈로그</h2>
        <div className="list">
          {catalog.map((g) => (
            <details key={g.gradeCode} className="panel" style={{ marginBottom: 10 }}>
              <summary>
                <strong>
                  {g.gradeLevel}. {g.gradeCode}
                </strong>{" "}
                {g.gradeName}
              </summary>
              <p className="hint">{g.description}</p>
              <h3>조건</h3>
              <ul className="plain-list">
                {(g.conditions || []).map((c) => (
                  <li key={`${g.gradeCode}-${c.metricType}`}>
                    {c.conditionName || c.metricType}: ≥ {c.thresholdValue}
                  </li>
                ))}
              </ul>
              <h3>혜택</h3>
              <BenefitList benefits={g.benefits} />
            </details>
          ))}
        </div>
      </section>
    </>
  );
}

function BenefitList({ benefits }) {
  if (!benefits?.length) return <p className="hint">혜택 없음</p>;
  return (
    <ul className="plain-list">
      {benefits.map((b) => (
        <li key={b.benefitCode}>
          <strong>{b.benefitCode}</strong>
          {b.benefitName ? ` · ${b.benefitName}` : ""}
          {b.benefitValue != null && b.benefitValue !== "" ? ` = ${b.benefitValue}` : ""}
          {b.description ? ` — ${b.description}` : ""}
        </li>
      ))}
    </ul>
  );
}
