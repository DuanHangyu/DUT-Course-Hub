import { dayjs } from "element-plus";

/**
 * 参数处理
 * @param {*} params  参数
 */
export function tansParams(params) {
  let result = "";
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    var part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof value !== "undefined") {
      if (typeof value === "object") {
        for (const key of Object.keys(value)) {
          if (
            value[key] !== null &&
            value[key] !== "" &&
            typeof value[key] !== "undefined"
          ) {
            let params = propName + "[" + key + "]";
            var subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result;
}

export function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return "";

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = Math.floor(seconds % 60);

  let result = "";
  if (hours > 0) {
    result += `${hours}时`;
  }
  if (minutes > 0) {
    result += `${minutes}分`;
  }
  if (secs > 0) {
    result += `${secs}秒`;
  }

  return result;
}

export function minutesToTime(minutes) {
  // 创建一天的开始时间 (00:00)
  const baseTime = dayjs().startOf("day");
  // 添加指定分钟数
  const resultTime = baseTime.add(minutes, "minute");
  // 格式化输出
  return resultTime.format("HH:mm");
}

export const indexToUpperCaseLetter = (index) => {
  return String.fromCharCode(65 + index); // 65 是 'A' 的 ASCII 码
};

export const numberToChinese = (num) => {
  if (num === 0) return "零";

  const chineseNum = [
    "零",
    "一",
    "二",
    "三",
    "四",
    "五",
    "六",
    "七",
    "八",
    "九",
  ];
  const chineseUnit = ["", "十", "百", "千", "万"];

  if (num < 10) {
    return chineseNum[num];
  }

  const numStr = num.toString();
  const len = numStr.length;

  if (len > 5) {
    // 处理大于9999的数字
    const wanPart = Math.floor(num / 10000);
    const remainder = num % 10000;
    if (remainder === 0) {
      return numberToChinese(wanPart) + "万";
    } else {
      return numberToChinese(wanPart) + "万" + numberToChinese(remainder);
    }
  }

  let result = "";
  for (let i = 0; i < len; i++) {
    const digit = parseInt(numStr[i]);
    const unit = chineseUnit[len - i - 1];

    if (digit !== 0) {
      result += chineseNum[digit] + unit;
    } else {
      // 处理中间的零
      if (result !== "" && !result.endsWith("零") && i < len - 1) {
        result += "零";
      }
    }
  }

  // 特殊处理十几的情况（如10应该显示为"十"而不是"一十"）
  if (num >= 10 && num < 20) {
    result = result.replace(/^一/, "");
  }

  // 移除末尾的零
  result = result.replace(/零$/, "");

  return result;
};
