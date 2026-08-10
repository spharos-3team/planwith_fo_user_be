import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../auth.jsx";

export default function Login() {
  const { login, isAuthed, booting } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (booting) return <div className="boot">PlanWith 불러오는 중…</div>;
  if (isAuthed) return <Navigate to="/me" replace />;

  return (
    <div className="panel-page">
      <form
        className="panel"
        onSubmit={async (e) => {
          e.preventDefault();
          setError("");
          setLoading(true);
          try {
            await login(email.trim(), password);
            navigate("/me");
          } catch (err) {
            setError(err.message || "로그인에 실패했습니다.");
          } finally {
            setLoading(false);
          }
        }}
      >
        <Link to="/" className="brand" style={{ color: "var(--ink)" }}>
          PlanWith
        </Link>
        <h1>다시 오신 걸 환영해요</h1>
        <p className="sub">이메일 계정으로 로그인하고 프로필·팔로우를 시험해 보세요.</p>
        {error && <div className="error">{error}</div>}
        <div className="field">
          <label htmlFor="email">이메일</label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div className="row" style={{ marginTop: 8 }}>
          <button className="btn btn-leaf" type="submit" disabled={loading}>
            {loading ? "로그인 중…" : "로그인"}
          </button>
          <Link className="btn btn-ghost dark" to="/signup">
            회원가입
          </Link>
        </div>
      </form>
    </div>
  );
}
