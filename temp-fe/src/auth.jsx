import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { api, getAccessToken, setAccessToken } from "./api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [profile, setProfile] = useState(null);
  const [booting, setBooting] = useState(true);

  const refreshProfile = useCallback(async () => {
    const me = await api("/api/v1/members/me");
    setProfile(me);
    return me;
  }, []);

  const applyTokens = useCallback(
    async (tokens) => {
      setAccessToken(tokens.accessToken);
      setUser(tokens.user || null);
      const me = await refreshProfile();
      return me;
    },
    [refreshProfile]
  );

  const login = useCallback(
    async (email, password) => {
      const tokens = await api("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      return applyTokens(tokens);
    },
    [applyTokens]
  );

  const logout = useCallback(async () => {
    try {
      await api("/api/v1/auth/logout", { method: "POST" });
    } catch {
      /* ignore */
    }
    setAccessToken(null);
    setUser(null);
    setProfile(null);
  }, []);

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        if (getAccessToken()) {
          try {
            const me = await api("/api/v1/members/me");
            if (!alive) return;
            setProfile(me);
            setUser({ userId: String(me.memberId) });
            return;
          } catch {
            setAccessToken(null);
          }
        }
        try {
          const tokens = await api("/api/v1/auth/refresh", { method: "POST" });
          if (!alive) return;
          await applyTokens(tokens);
        } catch {
          /* anonymous visitor */
        }
      } finally {
        if (alive) setBooting(false);
      }
    })();
    return () => {
      alive = false;
    };
  }, [applyTokens]);

  const value = useMemo(
    () => ({
      user,
      profile,
      booting,
      login,
      logout,
      applyTokens,
      refreshProfile,
      isAuthed: Boolean(profile && getAccessToken()),
    }),
    [user, profile, booting, login, logout, applyTokens, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
