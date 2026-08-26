import { useSearchParams } from "react-router-dom";

export function useUrlSelection<const Value extends string>(
  parameter: string,
  values: readonly Value[],
  defaultValue: Value,
) {
  const [searchParams, setSearchParams] = useSearchParams();
  const requestedValue = searchParams.get(parameter);
  const value = values.includes(requestedValue as Value)
    ? requestedValue as Value
    : defaultValue;

  function select(nextValue: Value, replace = false) {
    setSearchParams((current) => {
      const nextParams = new URLSearchParams(current);
      if (nextValue === defaultValue) nextParams.delete(parameter);
      else nextParams.set(parameter, nextValue);
      return nextParams;
    }, { replace });
  }

  return [value, select] as const;
}
