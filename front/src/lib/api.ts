const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

// 백엔드 공통 응답 포맷 (RsData)
export interface RsData<T> {
  resultCode: string;
  msg: string;
  data: T;
}

// 공통 API 호출 함수
export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<RsData<T>> {
  const res = await fetch(`${BASE_URL}/api/v1${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  const json: RsData<T> = await res.json();

  // 백엔드가 에러 응답(4xx, 5xx)을 보내면 예외로 던짐
  if (!res.ok) {
    throw new Error(json.msg || "요청에 실패했습니다.");
  }

  return json;
}