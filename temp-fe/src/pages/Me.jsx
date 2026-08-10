import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth.jsx";
import Avatar from "../components/Avatar.jsx";

export default function Me() {
  const { profile, refreshProfile } = useAuth();
  const navigate = useNavigate();
  const [editing, setEditing] = useState(false);
  const [nickname, setNickname] = useState(profile?.nickname || "");
  const [profileIntro, setProfileIntro] = useState(profile?.profileIntro || "");
  const [profileImage, setProfileImage] = useState(profile?.profileImage || "");
  const [followers, setFollowers] = useState([]);
  const [following, setFollowing] = useState([]);
  const [lookup, setLookup] = useState("");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!profile?.memberUuid) return;
    let alive = true;
    const memberUuid = profile.memberUuid;

    const syncFollows = async ({ silent = false } = {}) => {
      try {
        const [a, b] = await Promise.all([
          api(`/api/v1/members/${memberUuid}/followers`),
          api(`/api/v1/members/${memberUuid}/following`),
        ]);
        if (!alive) return;
        setFollowers(a || []);
        setFollowing(b || []);
        await refreshProfile();
      } catch (err) {
        if (!alive || silent) return;
        setError(err.message);
      }
    };

    syncFollows();
    const timer = setInterval(() => syncFollows({ silent: true }), 5000);
    return () => {
      alive = false;
      clearInterval(timer);
    };
  }, [profile?.memberUuid, refreshProfile]);

  if (!profile) return null;

  return (
    <>
      <section className="profile-head">
        <Avatar nickname={profile.nickname} profileImage={profile.profileImage} />
        <div>
          <h1>{profile.nickname}</h1>
          <p className="meta">
            {profile.grade} · {profile.email}
          </p>
          <p className="meta">{profile.profileIntro || "소개가 아직 없어요."}</p>
          <div className="stats">
            <div>
              {profile.followerCount} <span>팔로워</span>
            </div>
            <div>
              {profile.followingCount} <span>팔로잉</span>
            </div>
          </div>
          <p className="uuid">memberUuid: {profile.memberUuid}</p>
          <div className="row" style={{ marginTop: 14 }}>
            <button className="btn btn-leaf" type="button" onClick={() => setEditing((v) => !v)}>
              {editing ? "수정 닫기" : "프로필 수정"}
            </button>
            <button
              className="btn btn-ghost dark"
              type="button"
              onClick={() => navigator.clipboard.writeText(profile.memberUuid)}
            >
              UUID 복사
            </button>
          </div>
        </div>
      </section>

      {error && <div className="error">{error}</div>}
      {info && <div className="success">{info}</div>}

      {editing && (
        <section className="section panel" style={{ width: "100%", marginBottom: 24 }}>
          <h2>프로필 수정</h2>
          <div className="field">
            <label>닉네임</label>
            <input value={nickname} onChange={(e) => setNickname(e.target.value)} maxLength={10} />
          </div>
          <div className="field">
            <label>소개</label>
            <input value={profileIntro} onChange={(e) => setProfileIntro(e.target.value)} maxLength={20} />
          </div>
          <div className="field">
            <label>프로필 이미지 URL (빈 값이면 삭제)</label>
            <input value={profileImage ?? ""} onChange={(e) => setProfileImage(e.target.value)} />
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
                await api("/api/v1/members/me", {
                  method: "PATCH",
                  body: JSON.stringify({
                    nickname: nickname.trim() || null,
                    profileIntro,
                    profileImage,
                  }),
                });
                await refreshProfile();
                setInfo("프로필이 저장되었습니다.");
                setEditing(false);
              } catch (err) {
                setError(err.message);
              } finally {
                setLoading(false);
              }
            }}
          >
            저장
          </button>
        </section>
      )}

      <section className="section">
        <h2>다른 회원 찾아보기</h2>
        <p className="hint">
          팔로우 테스트를 위해 두 번째 계정으로 가입한 뒤, 그쪽 UUID를 여기에 붙여넣으세요.
        </p>
        <div className="find-box">
          <input
            placeholder="memberUuid"
            value={lookup}
            onChange={(e) => setLookup(e.target.value)}
          />
          <button
            className="btn btn-leaf"
            type="button"
            onClick={() => {
              const id = lookup.trim();
              if (id) navigate(`/m/${id}`);
            }}
          >
            열기
          </button>
        </div>
      </section>

      <section className="section">
        <h2>팔로워</h2>
        <MemberList items={followers} empty="아직 팔로워가 없습니다." />
      </section>

      <section className="section">
        <h2>팔로잉</h2>
        <MemberList items={following} empty="아직 팔로우한 회원이 없습니다." />
      </section>
    </>
  );
}

function MemberList({ items, empty }) {
  if (!items?.length) return <p className="hint">{empty}</p>;
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
          <span className="hint">
            {m.followerCount} 팔로워
          </span>
        </Link>
      ))}
    </div>
  );
}
