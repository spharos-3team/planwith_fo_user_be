import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth.jsx";
import Avatar from "../components/Avatar.jsx";

export default function Member() {
  const { memberUuid } = useParams();
  const { profile: me, refreshProfile } = useAuth();
  const [member, setMember] = useState(null);
  const [followers, setFollowers] = useState([]);
  const [following, setFollowing] = useState([]);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setError("");
    const [m, a, b] = await Promise.all([
      api(`/api/v1/members/${memberUuid}`),
      api(`/api/v1/members/${memberUuid}/followers`),
      api(`/api/v1/members/${memberUuid}/following`),
    ]);
    setMember(m);
    setFollowers(a || []);
    setFollowing(b || []);
  };

  useEffect(() => {
    let alive = true;

    const sync = async ({ silent = false } = {}) => {
      try {
        const [m, a, b] = await Promise.all([
          api(`/api/v1/members/${memberUuid}`),
          api(`/api/v1/members/${memberUuid}/followers`),
          api(`/api/v1/members/${memberUuid}/following`),
        ]);
        if (!alive) return;
        setMember(m);
        setFollowers(a || []);
        setFollowing(b || []);
      } catch (err) {
        if (!alive || silent) return;
        setError(err.message);
      }
    };

    sync();
    const timer = setInterval(() => sync({ silent: true }), 5000);
    return () => {
      alive = false;
      clearInterval(timer);
    };
  }, [memberUuid]);

  if (!member && !error) return <div className="hint">불러오는 중…</div>;
  if (!member) return <div className="error">{error}</div>;

  const isMe = me?.memberUuid === member.memberUuid;

  return (
    <>
      <p className="hint">
        <Link to="/me">← 내 프로필</Link>
      </p>
      <section className="profile-head">
        <Avatar nickname={member.nickname} profileImage={member.profileImage} />
        <div>
          <h1>{member.nickname}</h1>
          <p className="meta">{member.grade}</p>
          <p className="meta">{member.profileIntro || "소개가 아직 없어요."}</p>
          <div className="stats">
            <div>
              {member.followerCount} <span>팔로워</span>
            </div>
            <div>
              {member.followingCount} <span>팔로잉</span>
            </div>
          </div>
          <p className="uuid">{member.memberUuid}</p>
          {!isMe && (
            <div className="row" style={{ marginTop: 14 }}>
              {member.followedByMe ? (
                <button
                  className="btn btn-danger"
                  type="button"
                  disabled={loading}
                  onClick={async () => {
                    setLoading(true);
                    setError("");
                    setInfo("");
                    try {
                      await api(`/api/v1/members/${memberUuid}/follow`, { method: "DELETE" });
                      setInfo("언팔로우했습니다.");
                      await load();
                      await refreshProfile();
                    } catch (err) {
                      setError(err.message);
                    } finally {
                      setLoading(false);
                    }
                  }}
                >
                  언팔로우
                </button>
              ) : (
                <button
                  className="btn btn-leaf"
                  type="button"
                  disabled={loading}
                  onClick={async () => {
                    setLoading(true);
                    setError("");
                    setInfo("");
                    try {
                      await api(`/api/v1/members/${memberUuid}/follow`, { method: "POST" });
                      setInfo("팔로우했습니다.");
                      await load();
                      await refreshProfile();
                    } catch (err) {
                      setError(err.message);
                    } finally {
                      setLoading(false);
                    }
                  }}
                >
                  팔로우
                </button>
              )}
            </div>
          )}
        </div>
      </section>

      {error && <div className="error">{error}</div>}
      {info && <div className="success">{info}</div>}

      <section className="section">
        <h2>팔로워</h2>
        <MemberList items={followers} />
      </section>
      <section className="section">
        <h2>팔로잉</h2>
        <MemberList items={following} />
      </section>
    </>
  );
}

function MemberList({ items }) {
  if (!items?.length) return <p className="hint">목록이 비어 있습니다.</p>;
  return (
    <div className="list">
      {items.map((m) => (
        <Link key={m.memberUuid} className="list-item" to={`/m/${m.memberUuid}`}>
          <div className="list-item__left">
            <Avatar className="mini" nickname={m.nickname} profileImage={m.profileImage} />
            <div>
              <strong>{m.nickname}</strong>
              <div className="hint">{m.profileIntro || m.grade}</div>
            </div>
          </div>
        </Link>
      ))}
    </div>
  );
}
