export default function ScoreRing({ score, size = 44 }) {
    const color = score >= 70 ? "#34d399" : score >= 40 ? "#fbbf24" : "#94a3b8";
    const radius = size / 2 - 4;
    const circumference = 2 * Math.PI * radius;
    return (
        <span className="relative inline-flex items-center justify-center" style={{ width: size, height: size }} title={`Score ${score}`}>
            <svg width={size} height={size} className="-rotate-90">
                <circle cx={size / 2} cy={size / 2} r={radius} stroke="#22304f" strokeWidth="4" fill="none" />
                <circle
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    stroke={color}
                    strokeWidth="4"
                    fill="none"
                    strokeLinecap="round"
                    strokeDasharray={`${(score / 100) * circumference} ${circumference}`}
                />
            </svg>
            <span className="absolute text-xs font-semibold text-white">{score}</span>
        </span>
    );
}
