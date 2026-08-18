import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../auth.jsx";

export default function Home() {
  const { isAuthed, booting } = useAuth();
  if (booting) return <div className="boot">PlanWith 불러오는 중…</div>;
  if (isAuthed) return <Navigate to="/me" replace />;

  return (
    <div className="landing">
      <div className="landing__hero-media" aria-hidden="true" />
      <nav className="landing__nav">
        <div className="brand">PlanWith</div>
        <div className="landing__nav-actions">
          <Link className="btn btn-ghost" to="/login">
            로그인
          </Link>
          <Link className="btn btn-primary" to="/signup">
            시작하기
          </Link>
        </div>
      </nav>
      <section className="landing__content">
        <h1>함께 짜는 여행의 시작</h1>
        <p className="landing__lead">
          일정·취향·동선을 한곳에서 나누고, 마음에 드는 여행자와 연결하세요.
        </p>
        <div className="landing__cta">
          <Link className="btn btn-primary" to="/signup">
            무료로 가입하기
          </Link>
          <Link className="btn btn-ghost" to="/login">
            이미 계정이 있어요
          </Link>
        </div>
      </section>
    </div>
  );
}
