export default function MyPage() {
    // 백엔드 연동 전까지 쓸 가짜 데이터
    const user = {
      name: "홍길동",
      loginId: "exampleId",
      email: "example@naver.com",
    };
  
    const tickets = [
      {
        ticketId: 1,
        concertName: "흠뻑쇼 - 대전",
        seatNumber: "A-12",
        startDate: "2026-06-06",
        ticketPrice: 120000,
        status: "BOOKED",
      },
      {
        ticketId: 2,
        concertName: "2026 SUMMER LIVE",
        seatNumber: "B-3",
        startDate: "2026-08-10",
        ticketPrice: 150000,
        status: "BOOKED",
      },
    ];
  
    return (
      <div className="min-h-screen bg-gray-50 p-10">
        <div className="max-w-3xl mx-auto">
          {/* 상단 헤더 */}
          <div className="flex justify-between items-center mb-8">
            <h1 className="text-2xl font-bold text-gray-800">마이페이지</h1>
            <button className="text-sm text-gray-400 hover:text-red-500">
              회원탈퇴
            </button>
          </div>
  
          {/* 사용자 정보 카드 */}
          <div className="bg-white rounded-2xl shadow-sm p-8 mb-8">
            <h2 className="text-lg font-bold text-gray-700 mb-4">내 정보</h2>
            <div className="space-y-2 text-gray-600">
              <p><span className="inline-block w-20 text-gray-400">이름</span>{user.name}</p>
              <p><span className="inline-block w-20 text-gray-400">아이디</span>{user.loginId}</p>
              <p><span className="inline-block w-20 text-gray-400">이메일</span>{user.email}</p>
            </div>
          </div>
  
          {/* 내 티켓 목록 */}
          <div className="bg-white rounded-2xl shadow-sm p-8">
            <h2 className="text-lg font-bold text-gray-700 mb-4">내 티켓</h2>
            <div className="space-y-4">
              {tickets.map((ticket) => (
                <div
                  key={ticket.ticketId}
                  className="flex justify-between items-center border border-gray-100 rounded-xl p-4 hover:shadow-md transition"
                >
                  <div>
                    <p className="font-bold text-gray-800">{ticket.concertName}</p>
                    <p className="text-sm text-gray-500 mt-1">
                      {ticket.startDate} · 좌석 {ticket.seatNumber}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-blue-600">
                      {ticket.ticketPrice.toLocaleString()}원
                    </p>
                    <span className="inline-block mt-1 px-2 py-0.5 text-xs bg-green-100 text-green-700 rounded-full">
                      {ticket.status}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }