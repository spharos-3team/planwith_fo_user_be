const ACCESS_KEY = "planwith_access_token";

export function getAccessToken() {
  return sessionStorage.getItem(ACCESS_KEY);
}

export function setAccessToken(token) {
  if (token) sessionStorage.setItem(ACCESS_KEY, token);
  else sessionStorage.removeItem(ACCESS_KEY);
}

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (!(options.body instanceof FormData) && !headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }
  const token = getAccessToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(path, {
    ...options,
    headers,
    credentials: "include",
  });

  const json = await res.json().catch(() => null);
  if (!res.ok || (json && json.success === false)) {
    const err = new Error(json?.error?.message || `요청 실패 (${res.status})`);
    err.code = json?.error?.code;
    err.fieldErrors = json?.error?.fieldErrors;
    err.status = res.status;
    throw err;
  }
  return json?.data;
}

export function mediaUrl(path) {
  if (!path) return null;
  if (path.startsWith("http")) return path;
  return path;
}
