export default async function ConcertDetailPage({
    params,
  }: {
    params: Promise<{ id: string }>;
  }) {
    const { id } = await params;
  
    // 백엔드 연동 전 가짜 데이터
    const concert = {
      concertId: id,
      concertName: "2026 SUMMER LIVE",
      description: "올여름 최고의 페스티벌! 뜨거운 여름밤을 함께해요.",
      venueName: "올림픽 체조경기장",
      location: "서울시 송파구",
      prices: { VIP: 150000, R: 120000, S: 90000, A: 70000 },
    };
  
    return (
      <div className="min-h-screen bg-gray-50 p-10">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div className="flex flex-col md:flex-row">
              {/* 포스터 */}
              <div className="md:w-1/3 h-80 bg-gradient-to-br from-blue-200 to-indigo-300 flex items-center justify-center text-white font-bold text-xl">
                포스터
              </div>
  
              {/* 정보 */}
              <div className="p-8 flex-1">
                <h1 className="text-2xl font-bold text-gray-800 mb-2">
                  {concert.concertName}
                </h1>
                <p className="text-gray-500 mb-1">📍 {concert.venueName}</p>
                <p className="text-gray-400 text-sm mb-4">{concert.location}</p>
                <p className="text-gray-600 mb-6">{concert.description}</p>
  
                {/* 가격 */}
                <div className="border-t pt-4 mb-6">
                  <h2 className="font-bold text-gray-700 mb-2">좌석 등급별 가격</h2>
                  <div className="space-y-1 text-sm text-gray-600">
                    <p>VIP석 — {concert.prices.VIP.toLocaleString()}원</p>
                    <p>R석 — {concert.prices.R.toLocaleString()}원</p>
                    <p>S석 — {concert.prices.S.toLocaleString()}원</p>
                    <p>A석 — {concert.prices.A.toLocaleString()}원</p>
                  </div>
                </div>
  
                <button className="w-full p-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold transition">
                  예매하기
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }