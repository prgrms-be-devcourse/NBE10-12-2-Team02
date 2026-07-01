const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export interface RsData<T> {
  resultCode: string;
  msg: string;
  data: T;
}

let accessToken: string | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("auth-changed"));
  }
}

export function getAccessToken() {
  return accessToken;
}

export function decodeToken(): { id: number; name: string } | null {
  if (!accessToken) return null;
  try {
    const payload = accessToken.split(".")[1];
    const json = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return { id: json.id, name: json.name };
  } catch {
    return null;
  }
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<RsData<T>> {
  const res = await fetch(`${BASE_URL}/api/v1${path}`, {
    ...options,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
  });

  const newAuthHeader = res.headers.get("Authorization");
  if (newAuthHeader?.startsWith("Bearer ")) {
    setAccessToken(newAuthHeader.slice(7));
  }

  const json: RsData<T> = await res.json();

  if (!res.ok) {
    throw new Error(json.msg || "요청에 실패했습니다.");
  }

  return json;
}