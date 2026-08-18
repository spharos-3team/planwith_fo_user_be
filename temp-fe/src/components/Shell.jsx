import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth.jsx";

export default function Shell({ children }) {
  const { logout, profile } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="shell">
      <header className="shell__top">
        <div className="shell__inner">
          <NavLink to="/me" className="brand" style={{ color: "var(--ink)" }}>
            PlanWith
          </NavLink>
          <nav className="shell__nav">
            <NavLink to="/me" end>
              내 프로필
            </NavLink>
            <NavLink to="/grades">등급</NavLink>
            <span className="hint">{profile?.nickname}</span>
            <button
              className="btn btn-ghost dark"
              type="button"
              onClick={async () => {
                await logout();
                navigate("/");
              }}
            >
              로그아웃
            </button>
          </nav>
        </div>
      </header>
      <main className="shell__main">{children}</main>
    </div>
  );
}
