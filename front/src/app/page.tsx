import Link from "next/link";

export default function Home() {
  const concerts = [
    { concertId: 1, concertName: "2026 SUMMER LIVE", venueName: "올림픽 체조경기장", startDate: "2026-08-10", lowestPrice: 77000 },
    { concertId: 2, concertName: "흠뻑쇼 - 대전", venueName: "대전월드컵경기장", startDate: "2026-07-20", lowestPrice: 99000 },
    { concertId: 3, concertName: "재즈 페스티벌", venueName: "올림픽공원", startDate: "2026-09-05", lowestPrice: 55000 },
    { concertId: 4, concertName: "클래식 갈라쇼", venueName: "예술의전당", startDate: "2026-08-25", lowestPrice: 120000 },
  ];

  return (
    <div className="min-h-screen bg-gray-50 p-10">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-800 mb-8">
          🎫 TicketingGo
        </h1>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          {concerts.map((concert) => (
            <Link
              href={`/concerts/${concert.concertId}`}
              key={concert.concertId}
              className="bg-white rounded-2xl shadow-sm overflow-hidden hover:shadow-lg transition cursor-pointer"
            >
              <div className="h-48 bg-gradient-to-br from-blue-200 to-indigo-300 flex items-center justify-center text-white font-bold">
                포스터
              </div>

              <div className="p-4">
                <h2 className="font-bold text-gray-800 truncate">
                  {concert.concertName}
                </h2>
                <p className="text-sm text-gray-500 mt-1">{concert.venueName}</p>
                <p className="text-sm text-gray-400 mt-1">{concert.startDate}</p>
                <p className="text-blue-600 font-bold mt-2">
                  {concert.lowestPrice.toLocaleString()}원~
                </p>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}