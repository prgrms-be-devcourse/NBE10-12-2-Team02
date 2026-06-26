"use client";

import { useState } from "react";

export default function LoginPage() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = () => {
    if (loginId.trim() === "") {
      alert("아이디를 입력해주세요.");
      return;
    }
    if (password.trim() === "") {
      alert("비밀번호를 입력해주세요.");
      return;
    }

    // 여기까지 왔으면 둘 다 입력된 상태
    console.log("로그인 시도:", loginId, password);
    alert("로그인 시도! (나중에 여기서 API 호출)");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="w-96 p-10 bg-white rounded-2xl shadow-xl">
        <h1 className="text-3xl font-bold text-center mb-2 text-gray-800">
          TicketingGo 🎫
        </h1>
        <p className="text-center text-gray-400 text-sm mb-8">
          로그인하고 티켓을 예매하세요
        </p>

        <input
          type="text"
          placeholder="아이디"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          className="w-full p-3 mb-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-3 mb-6 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
        />

        <button
          onClick={handleLogin}
          className="w-full p-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold transition"
        >
          로그인
        </button>

        <p className="text-center text-sm text-gray-500 mt-6">
          아직 회원이 아니신가요?{" "}
          <span className="text-blue-600 font-semibold cursor-pointer hover:underline">
            회원가입
          </span>
        </p>
      </div>
    </div>
  );
}