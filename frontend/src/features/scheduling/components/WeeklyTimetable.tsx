export interface WeeklyTimetableEntry {
  id: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  title: string;
  context: string;
  detail: string;
  room: string;
  componentType: string;
}

const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SUNDAY"] as const;
const dayLabels = { MONDAY: "Monday", TUESDAY: "Tuesday", WEDNESDAY: "Wednesday", THURSDAY: "Thursday", FRIDAY: "Friday", SUNDAY: "Sunday" } as const;
const gridStart = 8 * 60;
const gridEnd = 18 * 60 + 30;
const hourLabels = Array.from({ length: 11 }, (_, index) => 8 + index);

function timeToMinutes(value: string): number {
  const [hours, minutes] = value.split(":").map(Number);
  return hours * 60 + minutes;
}

function assignLanes(entries: WeeklyTimetableEntry[]) {
  const laneEnds: number[] = [];
  return [...entries].sort((left, right) => left.startTime.localeCompare(right.startTime)).map((entry) => {
    const start = timeToMinutes(entry.startTime);
    let lane = laneEnds.findIndex((end) => end <= start);
    if (lane < 0) {
      lane = laneEnds.length;
      laneEnds.push(0);
    }
    laneEnds[lane] = timeToMinutes(entry.endTime);
    return { entry, lane };
  });
}

export function WeeklyTimetable({ entries }: { entries: WeeklyTimetableEntry[] }) {
  return <div className="timetable-scroll professor-timetable-scroll"><div className="weekly-timetable professor-weekly-timetable"><div className="timetable-time-header"><span>Days</span><div>{hourLabels.map((hour) => <span key={hour} style={{ left: `${((hour * 60 - gridStart) / (gridEnd - gridStart)) * 100}%` }}>{hour}h</span>)}</div></div>{days.map((day) => {
    const laidOut = assignLanes(entries.filter((entry) => entry.dayOfWeek === day));
    const laneCount = Math.max(1, ...laidOut.map((item) => item.lane + 1));
    return <div className="timetable-day-row" key={day}><strong>{dayLabels[day]}</strong><div className="timetable-day-track professor-day-track" style={{ minHeight: `${Math.max(92, laneCount * 92)}px` }}>{laidOut.map(({ entry, lane }) => {
      const start = timeToMinutes(entry.startTime);
      const end = timeToMinutes(entry.endTime);
      return <article className={`timetable-session timetable-session--${entry.componentType.toLowerCase()}`} key={entry.id} style={{ left: `${((start - gridStart) / (gridEnd - gridStart)) * 100}%`, top: `${lane * 88 + 4}px`, width: `${((end - start) / (gridEnd - gridStart)) * 100}%` }} title={entry.context}><strong>{entry.title}</strong><span>{entry.context}</span><span>{entry.detail}</span><small>{entry.room} · {entry.startTime.slice(0, 5)}–{entry.endTime.slice(0, 5)}</small></article>;
    })}</div></div>;
  })}</div></div>;
}
