import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth.jsx";

export default function Signup() {
  const { login, isAuthed, booting } = useAuth();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [terms, setTerms] = useState([]);
  const [agreed, setAgreed] = useState({});
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [codeSent, setCodeSent] = useState(false);
  const [devHint, setDevHint] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [introduction, setIntroduction] = useState("");
  const [profileImage, setProfileImage] = useState("");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api("/api/v1/terms")
      .then((data) => {
        setTerms(data || []);
        const init = {};
        (data || []).forEach((t) => {
          init[t.id] = false;
        });
        setAgreed(init);
      })
      .catch((err) => setError(err.message));
  }, []);

  if (booting) return <div className="boot">PlanWith 불러오는 중…</div>;
  if (isAuthed) return <Navigate to="/me" replace />;

  const requiredOk = terms.filter((t) => t.required).every((t) => agreed[t.id]);

  return (
    <div className="panel-page">
      <div className="panel wide">
        <Link to="/" className="brand" style={{ color: "var(--ink)" }}>
          PlanWith
        </Link>
        <h1>여행 동료 만들기</h1>
        <p className="sub">약관 → 이메일 인증 → 프로필 순으로 가입합니다. Gateway(:8000)로 실제 API를 호출합니다.</p>

        <div className="steps">
          {[1, 2, 3].map((n) => (
            <div key={n} className={`step-dot ${step >= n ? "on" : ""}`} />
          ))}
        </div>

        {error && <div className="error">{error}</div>}
        {info && <div className="success">{info}</div>}

        {step === 1 && (
          <>
            <div className="terms">
              {terms.map((t) => (
                <label key={t.id} className="term">
                  <input
                    type="checkbox"
                    checked={Boolean(agreed[t.id])}
                    onChange={(e) => setAgreed((prev) => ({ ...prev, [t.id]: e.target.checked }))}
                  />
                  <span>
                    {t.required ? "[필수] " : "[선택] "}
                    {t.title}{" "}
                    <a href={t.contentUrl} target="_blank" rel="noreferrer">
                      보기
                    </a>
                  </span>
                </label>
              ))}
            </div>
            <button
              className="btn btn-leaf"
              type="button"
              disabled={!requiredOk}
              onClick={() => {
                setError("");
                setStep(2);
              }}
            >
              다음
            </button>
          </>
        )}

        {step === 2 && (
          <>
            <div className="field">
              <label>이메일</label>
              <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" />
            </div>
            <div className="row">
              <button
                className="btn btn-ghost dark"
                type="button"
                disabled={loading || !email}
                onClick={async () => {
                  setLoading(true);
                  setError("");
                  setInfo("");
                  setDevHint("");
                  try {
                    const dup = await api(`/api/v1/auth/check-email?email=${encodeURIComponent(email.trim())}`);
                    if (dup) throw new Error("이미 사용 중인 이메일입니다.");
                    await api("/api/v1/auth/email/send", {
                      method: "POST",
                      body: JSON.stringify({ email: email.trim() }),
                    });
                    setCodeSent(true);
                    setInfo("인증 코드를 보냈습니다.");
                    try {
                      const c = await api(`/api/v1/dev/email-code?email=${encodeURIComponent(email.trim())}`);
                      if (c) {
                        setDevHint(String(c));
                        setCode(String(c));
                      }
                    } catch {
                      /* real mail mode */
                    }
                  } catch (err) {
                    setError(err.message);
                  } finally {
                    setLoading(false);
                  }
                }}
              >
                인증코드 받기
              </button>
            </div>
            {devHint && <p className="hint">로컬 개발용 코드 자동입력: {devHint}</p>}
            <div className="field">
              <label>인증 코드</label>
              <input value={code} onChange={(e) => setCode(e.target.value)} disabled={!codeSent && !devHint} />
            </div>
            <div className="row">
              <button className="btn btn-ghost dark" type="button" onClick={() => setStep(1)}>
                이전
              </button>
              <button
                className="btn btn-leaf"
                type="button"
                disabled={loading || !email || !code}
                onClick={async () => {
                  setLoading(true);
                  setError("");
                  try {
                    await api("/api/v1/auth/email/verify", {
                      method: "POST",
                      body: JSON.stringify({ email: email.trim(), code: code.trim() }),
                    });
                    setInfo("이메일 인증 완료");
                    setStep(3);
                  } catch (err) {
                    setError(err.message);
                  } finally {
                    setLoading(false);
                  }
                }}
              >
                인증하고 다음
              </button>
            </div>
          </>
        )}

        {step === 3 && (
          <>
            <div className="field">
              <label>닉네임 (2~10자)</label>
              <input value={nickname} onChange={(e) => setNickname(e.target.value)} maxLength={10} />
            </div>
            <div className="field">
              <label>비밀번호 (영문+특수문자 8~20자)</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
              />
            </div>
            <div className="field">
              <label>소개 (최대 20자)</label>
              <input
                value={introduction}
                onChange={(e) => setIntroduction(e.target.value)}
                maxLength={20}
              />
            </div>
            <div className="field">
              <label>프로필 이미지 (400×400 jpg/png/webp, 선택)</label>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={async (e) => {
                  const file = e.target.files?.[0];
                  if (!file) return;
                  setLoading(true);
                  setError("");
                  try {
                    const fd = new FormData();
                    fd.append("file", file);
                    const url = await api("/api/v1/auth/profile-image", { method: "POST", body: fd });
                    setProfileImage(url);
                    setInfo("이미지 업로드 완료");
                  } catch (err) {
                    setError(err.message);
                  } finally {
                    setLoading(false);
                  }
                }}
              />
              {profileImage && <p className="hint">업로드됨: {profileImage}</p>}
            </div>
            <div className="row">
              <button className="btn btn-ghost dark" type="button" onClick={() => setStep(2)}>
                이전
              </button>
              <button
                className="btn btn-leaf"
                type="button"
                disabled={loading}
                onClick={async () => {
                  setLoading(true);
                  setError("");
                  try {
                    const nickDup = await api(
                      `/api/v1/auth/check-nickname?nickname=${encodeURIComponent(nickname.trim())}`
                    );
                    if (nickDup) throw new Error("이미 사용 중인 닉네임입니다.");
                    const agreedTermIds = terms.filter((t) => agreed[t.id]).map((t) => t.id);
                    await api("/api/v1/auth/signup", {
                      method: "POST",
                      body: JSON.stringify({
                        email: email.trim(),
                        password,
                        nickname: nickname.trim(),
                        profileImage: profileImage || null,
                        introduction: introduction.trim() || null,
                        agreedTermIds,
                      }),
                    });
                    await login(email.trim(), password);
                    navigate("/me");
                  } catch (err) {
                    setError(err.message);
                  } finally {
                    setLoading(false);
                  }
                }}
              >
                {loading ? "가입 중…" : "가입 완료"}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
