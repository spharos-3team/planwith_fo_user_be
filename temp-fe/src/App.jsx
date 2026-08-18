import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth.jsx";
import Home from "./pages/Home.jsx";
import Login from "./pages/Login.jsx";
import Signup from "./pages/Signup.jsx";
import Me from "./pages/Me.jsx";
import Member from "./pages/Member.jsx";
import Grades from "./pages/Grades.jsx";
import Shell from "./components/Shell.jsx";

function Private({ children }) {
  const { isAuthed, booting } = useAuth();
  if (booting) return <div className="boot">PlanWith 불러오는 중…</div>;
  if (!isAuthed) return <Navigate to="/login" replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route
        path="/me"
        element={
          <Private>
            <Shell>
              <Me />
            </Shell>
          </Private>
        }
      />
      <Route
        path="/m/:memberUuid"
        element={
          <Private>
            <Shell>
              <Member />
            </Shell>
          </Private>
        }
      />
      <Route
        path="/grades"
        element={
          <Private>
            <Shell>
              <Grades />
            </Shell>
          </Private>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
