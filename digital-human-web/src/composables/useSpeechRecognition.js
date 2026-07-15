import { ref } from "vue";

export function useSpeechRecognition() {
  const transcriptionText = ref("");
  let ws = null;
  let audioContext = null;
  let micNode = null;
  let audioStream = null;
  let isStreaming = false;

  const audioChunks = [];
  let sendInterval = null;
  let heartbeatInterval = null;

  function mergeChunks(chunks) {
    let totalLength = 0;
    chunks.forEach((chunk) => (totalLength += chunk.length));
    const merged = new Uint8Array(totalLength);
    let offset = 0;
    chunks.forEach((chunk) => {
      merged.set(chunk, offset);
      offset += chunk.length;
    });
    return merged;
  }

  function startAudio() {
    isStreaming = true;
    if (!sendInterval && ws && ws.readyState === WebSocket.OPEN) {
      sendInterval = setInterval(() => {
        if (audioChunks.length > 0 && ws.readyState === WebSocket.OPEN) {
          const merged = mergeChunks(audioChunks);
          console.log("ws.send");
          ws.send(merged);
          audioChunks.length = 0;
        }
      }, 100);
    }
  }

  function stopAudio() {
    isStreaming = false;
    if (sendInterval) {
      clearInterval(sendInterval);
      sendInterval = null;
    }
    audioChunks.length = 0;
  }

  function startHeartbeat() {
    if (heartbeatInterval || !ws || ws.readyState !== WebSocket.OPEN) return;
    heartbeatInterval = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        try {
          ws.send(JSON.stringify({ type: "ping" }));
          console.log("send heartbeat");
        } catch (e) {
          console.warn("Failed to send heartbeat", e);
        }
      }
    }, 5000); // 每 5 秒发送一次心跳
  }

  function stopHeartbeat() {
    if (heartbeatInterval) {
      clearInterval(heartbeatInterval);
      heartbeatInterval = null;
    }
  }

  async function initAudio() {
    try {
      audioStream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioContext = new AudioContext({ sampleRate: 16000 });

      await audioContext.audioWorklet.addModule("processor.js");

      const source = audioContext.createMediaStreamSource(audioStream);
      micNode = new AudioWorkletNode(audioContext, "mic-processor");

      micNode.port.onmessage = (event) => {
        if (!isStreaming) return;
        const chunk = new Uint8Array(event.data);
        audioChunks.push(chunk);
      };

      source.connect(micNode);

      ws = new WebSocket(`${import.meta.env.VITE_APP_WEBSOCKET_BASE_URL}/audio`);

      ws.onopen = () => {
        startHeartbeat();
      };

      ws.onmessage = (event) => {
        try {
          const result = event.data;
          if (result) {
            transcriptionText.value += result + " ";
          }
        } catch (e) {
          console.error("onmessage", e);
        }
      };

      ws.onerror = (e) => {
        console.error("WebSocket error", e);
      };

      ws.onclose = () => {
        stopHeartbeat();
      };
    } catch (err) {
      console.error("麦克风打开失败：", err);
    }
  }

  async function closeAudio() {
    try {
      stopAudio();
      stopHeartbeat();

      if (audioStream) {
        audioStream.getTracks().forEach((track) => track.stop());
        audioStream = null;
      }

      if (micNode) {
        micNode.disconnect();
        micNode = null;
      }

      if (audioContext) {
        if (audioContext.state !== "closed") {
          await audioContext.close();
        }
        audioContext = null;
      }

      if (ws) {
        if (ws.readyState !== WebSocket.CLOSED) {
          ws.close(1000, "Client close");
        }
        ws = null;
      }

      audioChunks.length = 0;
    } catch (err) {
      console.error("停止音频资源出错：", err);
    }
  }

  function warmup() {
    return new Promise((resolve) => {
      const warmupWs = new WebSocket(
        `${import.meta.env.VITE_APP_WEBSOCKET_BASE_URL}/audio`
      );
      warmupWs.onopen = () => {
        const silence = new Int16Array(48000 * 3).fill(0);
        warmupWs.send(new Uint8Array(silence.buffer));
        setTimeout(() => {
          if (warmupWs.readyState === WebSocket.OPEN) {
            warmupWs.close(1000, "Warmup done");
          }
          resolve();
        }, 3000);
      };
      warmupWs.onerror = (e) => {
        console.error("WebSocket warmup error", e);
        resolve();
      };
    });
  }

  return {
    transcriptionText,
    startAudio,
    stopAudio,
    initAudio,
    closeAudio,
    warmup,
    startHeartbeat,
    stopHeartbeat,
  };
}
