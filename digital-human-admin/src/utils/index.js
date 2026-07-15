export function useNumberFormat() {
  const format = (num) => {
    return num?.toLocaleString("en-US") || "0";
  };
  return { format };
}
