export default function SignupPage() {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="w-96 p-10 bg-white rounded-2xl shadow-xl">
          <h1 className="text-3xl font-bold text-center mb-8 text-gray-800">
            회원가입
          </h1>
  
          <input
            type="text"
            placeholder="사용자 이름"
            className="w-full p-3 mb-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <input
            type="email"
            placeholder="이메일"
            className="w-full p-3 mb-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
  
          <div className="flex gap-2 mb-3">
            <input
              type="text"
              placeholder="아이디"
              className="flex-1 p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
            />
            <button className="px-4 bg-gray-100 hover:bg-gray-200 rounded-lg text-sm font-semibold whitespace-nowrap">
              중복확인
            </button>
          </div>
  
          <input
            type="password"
            placeholder="비밀번호"
            className="w-full p-3 mb-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <input
            type="password"
            placeholder="비밀번호 확인"
            className="w-full p-3 mb-6 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
  
          <button className="w-full p-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold transition">
            회원가입 하기
          </button>
        </div>
      </div>
    );
  }