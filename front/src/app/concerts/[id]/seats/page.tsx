"use client";

import { useState, useEffect, use } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { apiFetch, decodeToken } from "@/lib/api";

interface SeatDetail {
  seatNumber: string;
  seatStatus: "AVAILABLE" | "HOLD" | "SOLD_OUT";
  gradeName: string;
}

interface SeatSelectionData {
  concertId: number;
  scheduleId: number;
  prices: Record<string, number>;
  seats: SeatDetail[];
}

export default function SeatSelectPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const searchParams = useSearchParams();
  const scheduleId = searchParams.get("scheduleId");
  const router = useRouter();

  const [seatData, setSeatData] = useState<SeatSelectionData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [isReserving, setIsReserving] = useState(false);

  useEffect(() => {
    if (!decodeToken()) {
      alert("로그인이 필요합니다.");
      router.push("/login");
      return;
    }

    if (!scheduleId) {
      setError("회차 정보가 없습니다.");
      setLoading(false);
      return;
    }

    let active = true;

    const fetchSeats = async () => {
      try {
        const res = await apiFetch<SeatSelectionData>(
          `/concerts/${id}/schedules/${scheduleId}/seats`
        );
        if (active) {
          setSeatData(res.data);
          setError("");
        }
      } catch (e) {
        if (active) {
          setError(e instanceof Error ? e.message : "좌석 정보를 불러오지 못했습니다.");
        }
      } finally {
        if (active) setLoading(false);
      }
    };

    fetchSeats();
    const intervalId = setInterval(fetchSeats, 3000);

    return () => {
      active = false;
      clearInterval(intervalId);
    };
  }, [id, scheduleId]);

  const seatStatusMap = new Map(seatData?.seats.map((s) => [s.seatNumber, s.seatStatus]) ?? []);
  const seatGradeMap = new Map(seatData?.seats.map((s) => [s.seatNumber, s.gradeName]) ?? []);

  const rows = Array.from(
    new Set(seatData?.seats.map((s) => s.seatNumber.split("-")[0]) ?? [])
  ).sort();

  const handleSeatClick = (seatNumber: string) => {
    const status = seatStatusMap.get(seatNumber);
    if (status !== "AVAILABLE") return;

    if (selectedSeats.includes(seatNumber)) {
      setSelectedSeats(selectedSeats.filter((seat) => seat !== seatNumber));
    } else {
      setSelectedSeats([...selectedSeats, seatNumber]);
    }
  };

  const totalPrice = selectedSeats.reduce((sum, seatNumber) => {
    const grade = seatGradeMap.get(seatNumber);
    const price = grade ? seatData?.prices[grade] ?? 0 : 0;
    return sum + price;
  }, 0);

  const handleProceedToPayment = async () => {
    if (selectedSeats.length !== 1) {
      alert("결제는 한 번에 좌석 1개만 가능합니다. (백엔드 제약)");
      return;
    }

    const seatNumber = selectedSeats[0];
    setIsReserving(true);
    try {
      const res = await apiFetch<{ occupyToken: string; expireInSeconds: number }>(
        `/concerts/${id}/schedules/${scheduleId}/seats/occupy`,
        { method: "POST", body: JSON.stringify({ seatNumber }) }
      );

      const grade = seatGradeMap.get(seatNumber);
      const price = grade ? seatData?.prices[grade] ?? 0 : 0;

      const params = new URLSearchParams({
        concertId: id,
        scheduleId: scheduleId ?? "",
        seatNumber,
        occupyToken: res.data.occupyToken,
        price: String(price),
      });
      router.push(`/payment?${params.toString()}`);
    } catch (e) {
      alert(e instanceof Error ? e.message : "좌석 선점에 실패했습니다.");
    } finally {
      setIsReserving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400">불러오는 중...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-red-400">{error}</p>
      </div>
    );
  }

  const seatsByRow = (row: string) =>
    (seatData?.seats ?? [])
      .filter((s) => s.seatNumber.startsWith(`${row}-`))
      .sort((a, b) => parseInt(a.seatNumber.split("-")[1]) - parseInt(b.seatNumber.split("-")[1]));

  return (
    <div className="min-h-screen bg-gray-50 p-10">
      <div className="max-w-3xl mx-auto">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">좌석 선택</h1>

        <div className="bg-gray-700 text-white text-center py-3 rounded-lg mb-8 font-bold">
          STAGE
        </div>

        <div className="bg-white rounded-2xl shadow-sm p-8">
          <div className="space-y-3">
            {rows.map((row) => (
              <div key={row} className="flex items-center gap-3 justify-center">
                <span className="w-6 font-bold text-gray-400">{row}</span>
                <div className="flex gap-2">
                  {seatsByRow(row).map((seat) => {
                    const isSelected = selectedSeats.includes(seat.seatNumber);
                    const col = seat.seatNumber.split("-")[1];

                    let seatClass = "";
                    if (seat.seatStatus === "SOLD_OUT" || seat.seatStatus === "HOLD") {
                      seatClass = "bg-gray-300 text-gray-400 cursor-not-allowed";
                    } else if (isSelected) {
                      seatClass = "bg-yellow-400 text-white cursor-pointer";
                    } else {
                      seatClass = "bg-blue-100 text-blue-700 hover:bg-blue-300 cursor-pointer";
                    }

                    return (
                      <div
                        key={seat.seatNumber}
                        onClick={() => handleSeatClick(seat.seatNumber)}
                        className={`w-8 h-8 rounded flex items-center justify-center text-xs font-semibold ${seatClass}`}
                      >
                        {col}
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>

          <div className="flex gap-6 justify-center mt-8 text-sm text-gray-500 flex-wrap">
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 rounded bg-blue-100"></div> 선택 가능
            </div>
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 rounded bg-yellow-400"></div> 선택됨
            </div>
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 rounded bg-gray-300"></div> 예매 완료
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-sm p-6 mt-6">
          <h2 className="font-bold text-gray-700 mb-2">선택한 좌석</h2>
          {selectedSeats.length === 0 ? (
            <p className="text-gray-400 text-sm">좌석을 선택해주세요.</p>
          ) : (
            <>
              <p className="text-blue-600 font-semibold mb-2">
                {selectedSeats.join(", ")} ({selectedSeats.length}석)
              </p>
              <div className="flex justify-between items-center border-t pt-3 mb-4">
                <span className="text-gray-600">총 결제 금액</span>
                <span className="text-xl font-bold text-blue-600">{totalPrice.toLocaleString()}원</span>
              </div>
              <button
                onClick={handleProceedToPayment}
                disabled={isReserving}
                className="block w-full p-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-bold text-center transition disabled:opacity-50"
              >
                {isReserving ? "선점 중..." : `선택 완료 (${selectedSeats.length}석)`}
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}