<template>
  <div class="w-full h-full">
    <video
      ref="videoRef"
      id="video"
      autoplay
      muted
      loop
      preload="auto"
      disablePictureInPicture
    >
      <!-- <source src="/assets/0619_1.mov" type="video/mov" /> -->
    </video>
    <canvas
      id="canvas_video"
      ref="canvas_video"
      v-show="!hideHuman"
      :style="{ width: width, height: height }"
    ></canvas>
    <canvas id="canvas_gl" width="128" height="128" ref="canvas_gl"></canvas>

    <div id="screen"></div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, defineEmits } from "vue";
import * as mat4 from "gl-matrix/mat4";
import { ElMessage } from "element-plus";
import pako from "pako"; // 引入pako
import PCMPlayer from "pcm-player";
import { getToken } from "@/utils/auth";
let player = new PCMPlayer({
  encoding: "16bitInt", // 采样位数
  channels: 1, // 通道
  sampleRate: 16000, // 采样率
  flushingTime: 2000, // pcm刷新间隔
  onstatechange: (node, event, type) => {
    console.log("onstatechange");
  }, // 播放状态变化事件
  onended: (node, event) => {
    // isPlaying = false;
    console.log("onended");

    // if (audioQueue.length > 0) {
    //   PlayWav();
    // }
  }, // 播放结束事件
});
const props = defineProps({
  width: {
    type: String,
  },
  height: {
    type: String,
  },
  hideHuman: {
    type: Boolean,
    default: false,
  },
});

let socket = null;
let socketVoice = null;

let isStop = false; //停止回答

let audioContext; // 音频上下文
let scriptProcessor;
let isPlaying = false; // 是否正在播放
let IsRecogition = true; // 是否正在识别
let currentInputModelIsVoice = true; // 当前输入模式是否是语音
let microphoneStream = null; // 麦克风流
let audioQueue = [];
const canvas_video = ref(null);
let ctx_video = null; // 用于存储 2D 上下文
let gl = null;
let texture_bs = null;
const videoRef = ref(null);
const pixels_fbo = new Uint8Array(128 * 128 * 4);
let resizedCanvas;
let resizedCtx = null;

let dataSets = [];
let objData;
const canvas_gl = ref(null);
let program;
let indexBuffer;

let textContent = "";
const emit = defineEmits([
  "ready",
  "answer",
  "translate",
  "beforeReady",
  "testQuestion",
  "micFail",
  "micReady",
  "testQuestionLast",
]);

function initCtrl() {
  ctx_video = canvas_video.value.getContext("2d");
  resizedCanvas = document.createElement("canvas");
  resizedCtx = resizedCanvas.getContext("2d", { willReadFrequently: true });
  audioContext = new AudioContext();
  scriptProcessor = audioContext.createScriptProcessor(4096, 1, 1);
}

async function init() {
  const instance = await Load1({
    qt: {
      entryFunction: window.createQtAppInstance,
      containerElements: [screen],
    },
  });

  await Load2({
    qt: {
      entryFunction2: window.createQtAppInstance2,
      containerElements: [screen],
    },
  });
  await init_opengl();
  // emit("beforeReady", "init_opengl完成");
  initCtrl();
  // emit("beforeReady", "initCtrl完成");
  Initialized();
  // emit("beforeReady", "Initialized完成");
}

async function Load1(config) {
  const throwIfEnvUsedButNotExported = (instance, config) => {
    const environment = config.environment;
    if (!environment || Object.keys(environment).length === 0) return;
    const isEnvExported = typeof instance.ENV === "object";
    if (!isEnvExported)
      throw new Error(
        "ENV must be exported if environment variables are passed",
      );
  };

  const throwIfFsUsedButNotExported = (instance, config) => {
    const environment = config.environment;
    if (!environment || Object.keys(environment).length === 0) return;
    const isFsExported = typeof instance.FS === "object";
    if (!isFsExported)
      throw new Error("FS must be exported if preload is used");
  };

  if (typeof config !== "object")
    throw new Error("config is required, expected an object");
  if (typeof config.qt !== "object")
    throw new Error("config.qt is required, expected an object");
  if (typeof config.qt.entryFunction !== "function")
    config.qt.entryFunction = window.createQtAppInstance;

  config.qt.qtdir ??= "qt";
  config.qt.preload ??= [];

  config.qtContainerElements = config.qt.containerElements;
  delete config.qt.containerElements;
  config.qtFontDpi = config.qt.fontDpi;
  delete config.qt.fontDpi;

  // Used for rejecting a failed load's promise where emscripten itself does not allow it,
  // like in instantiateWasm below. This allows us to throw in case of a load error instead of
  // hanging on a promise to entry function, which emscripten unfortunately does.
  let circuitBreakerReject;
  const circuitBreaker = new Promise((_, reject) => {
    circuitBreakerReject = reject;
  });

  // If module async getter is present, use it so that module reuse is possible.
  if (config.qt.module) {
    config.instantiateWasm = async (imports, successCallback) => {
      try {
        const module = await config.qt.module;
        successCallback(await WebAssembly.instantiate(module, imports), module);
      } catch (e) {
        circuitBreakerReject(e);
      }
    };
  }

  const qtPreRun = (instance) => {
    // Copy qt.environment to instance.ENV
    throwIfEnvUsedButNotExported(instance, config);
    for (const [name, value] of Object.entries(config.qt.environment ?? {}))
      instance.ENV[name] = value;

    // Copy self.preloadData to MEMFS
    const makeDirs = (FS, filePath) => {
      const parts = filePath.split("/");
      let path = "/";
      for (let i = 0; i < parts.length - 1; ++i) {
        const part = parts[i];
        if (part == "") continue;
        path += part + "/";
        try {
          FS.mkdir(path);
        } catch (error) {
          const EEXIST = 20;
          if (error.errno != EEXIST) throw error;
        }
      }
    };
    throwIfFsUsedButNotExported(instance, config);
    for ({ destination, data } of self.preloadData) {
      makeDirs(instance.FS, destination);
      instance.FS.writeFile(destination, new Uint8Array(data));
    }
  };

  if (!config.preRun) config.preRun = [];
  config.preRun.push(qtPreRun);

  config.onRuntimeInitialized = () => config.qt.onLoaded?.();

  const originalLocateFile = config.locateFile;
  config.locateFile = (filename) => {
    const originalLocatedFilename = originalLocateFile
      ? originalLocateFile(filename)
      : filename;
    if (originalLocatedFilename.startsWith("libQt6"))
      return `${config.qt.qtdir}/lib/${originalLocatedFilename}`;
    return originalLocatedFilename;
  };

  const originalOnExit = config.onExit;
  config.onExit = (code) => {
    originalOnExit?.();
    config.qt.onExit?.({
      code,
      crashed: false,
    });
  };

  const originalOnAbort = config.onAbort;
  config.onAbort = (text) => {
    originalOnAbort?.();

    aborted = true;
    config.qt.onExit?.({
      text,
      crashed: true,
    });
  };

  const fetchPreloadFiles = async () => {
    const fetchJson = async (path) => (await fetch(path)).json();
    const fetchArrayBuffer = async (path) => (await fetch(path)).arrayBuffer();
    const loadFiles = async (paths) => {
      const source = paths["source"].replace("$QTDIR", config.qt.qtdir);
      return {
        destination: paths["destination"],
        data: await fetchArrayBuffer(source),
      };
    };
    const fileList = (
      await Promise.all(config.qt.preload.map(fetchJson))
    ).flat();
    self.preloadData = (await Promise.all(fileList.map(loadFiles))).flat();
  };

  await fetchPreloadFiles();

  // Call app/emscripten module entry function. It may either come from the emscripten
  // runtime script or be customized as needed.
  let instance;
  try {
    instance = await Promise.race([
      circuitBreaker,
      config.qt.entryFunction(config),
    ]);
  } catch (e) {
    config.qt.onExit?.({
      text: e.message,
      crashed: true,
    });
    throw e;
  }

  return instance;
}

async function Load2(config) {
  const throwIfEnvUsedButNotExported = (instance, config) => {
    const environment = config.environment;
    if (!environment || Object.keys(environment).length === 0) return;
    const isEnvExported = typeof instance.ENV === "object";
    if (!isEnvExported)
      throw new Error(
        "ENV must be exported if environment variables are passed",
      );
  };

  const throwIfFsUsedButNotExported = (instance, config) => {
    const environment = config.environment;
    if (!environment || Object.keys(environment).length === 0) return;
    const isFsExported = typeof instance.FS === "object";
    if (!isFsExported)
      throw new Error("FS must be exported if preload is used");
  };

  if (typeof config !== "object")
    throw new Error("config is required, expected an object");
  if (typeof config.qt !== "object")
    throw new Error("config.qt is required, expected an object");
  if (typeof config.qt.entryFunction2 !== "function")
    config.qt.entryFunction2 = window.createQtAppInstance2;

  config.qt.qtdir ??= "qt";
  config.qt.preload ??= [];

  config.qtContainerElements = config.qt.containerElements;
  delete config.qt.containerElements;
  config.qtFontDpi = config.qt.fontDpi;
  delete config.qt.fontDpi;

  // Used for rejecting a failed load's promise where emscripten itself does not allow it,
  // like in instantiateWasm below. This allows us to throw in case of a load error instead of
  // hanging on a promise to entry function, which emscripten unfortunately does.
  let circuitBreakerReject;
  const circuitBreaker = new Promise((_, reject) => {
    circuitBreakerReject = reject;
  });

  // If module async getter is present, use it so that module reuse is possible.
  if (config.qt.module) {
    config.instantiateWasm = async (imports, successCallback) => {
      try {
        const module = await config.qt.module;
        successCallback(await WebAssembly.instantiate(module, imports), module);
      } catch (e) {
        circuitBreakerReject(e);
      }
    };
  }

  const qtPreRun = (instance) => {
    // Copy qt.environment to instance.ENV
    throwIfEnvUsedButNotExported(instance, config);
    for (const [name, value] of Object.entries(config.qt.environment ?? {}))
      instance.ENV[name] = value;

    // Copy self.preloadData to MEMFS
    const makeDirs = (FS, filePath) => {
      const parts = filePath.split("/");
      let path = "/";
      for (let i = 0; i < parts.length - 1; ++i) {
        const part = parts[i];
        if (part == "") continue;
        path += part + "/";
        try {
          FS.mkdir(path);
        } catch (error) {
          const EEXIST = 20;
          if (error.errno != EEXIST) throw error;
        }
      }
    };
    throwIfFsUsedButNotExported(instance, config);
    for ({ destination, data } of self.preloadData) {
      makeDirs(instance.FS, destination);
      instance.FS.writeFile(destination, new Uint8Array(data));
    }
  };

  if (!config.preRun) config.preRun = [];
  config.preRun.push(qtPreRun);

  config.onRuntimeInitialized = () => config.qt.onLoaded?.();

  const originalLocateFile = config.locateFile;
  config.locateFile = (filename) => {
    const originalLocatedFilename = originalLocateFile
      ? originalLocateFile(filename)
      : filename;
    if (originalLocatedFilename.startsWith("libQt6"))
      return `${config.qt.qtdir}/lib/${originalLocatedFilename}`;
    return originalLocatedFilename;
  };

  const originalOnExit = config.onExit;
  config.onExit = (code) => {
    originalOnExit?.();
    config.qt.onExit?.({
      code,
      crashed: false,
    });
  };

  const originalOnAbort = config.onAbort;
  config.onAbort = (text) => {
    originalOnAbort?.();

    aborted = true;
    config.qt.onExit?.({
      text,
      crashed: true,
    });
  };

  const fetchPreloadFiles = async () => {
    const fetchJson = async (path) => (await fetch(path)).json();
    const fetchArrayBuffer = async (path) => (await fetch(path)).arrayBuffer();
    const loadFiles = async (paths) => {
      const source = paths["source"].replace("$QTDIR", config.qt.qtdir);
      return {
        destination: paths["destination"],
        data: await fetchArrayBuffer(source),
      };
    };
    const fileList = (
      await Promise.all(config.qt.preload.map(fetchJson))
    ).flat();
    self.preloadData = (await Promise.all(fileList.map(loadFiles))).flat();
  };

  await fetchPreloadFiles();

  // Call app/emscripten module entry function. It may either come from the emscripten
  // runtime script or be customized as needed.
  let instance;
  try {
    instance = await Promise.race([
      circuitBreaker,
      config.qt.entryFunction2(config),
    ]);
  } catch (e) {
    config.qt.onExit?.({
      text: e.message,
      crashed: true,
    });
    throw e;
  }

  return instance;
}

function SetModule(module, combinedData) {
  // 剔除 json_data 字段
  let { json_data, ...WasmInputJson } = combinedData;

  let jsonString = JSON.stringify(WasmInputJson);
  // 分配内存
  // 使用 TextEncoder 计算 UTF-8 字节长度
  function getUTF8Length(str) {
    const encoder = new TextEncoder();
    const encoded = encoder.encode(str);
    return encoded.length + 1; // +1 是为了包含 null 终止符
  }
  let lengthBytes = getUTF8Length(jsonString);

  let stringPointer = module._malloc(lengthBytes);
  module.stringToUTF8(jsonString, stringPointer, lengthBytes);
  //        Module["asm"]["stringToUTF8"](jsonString, stringPointer, lengthBytes);

  module._processJson(stringPointer);

  // 释放内存
  module._free(stringPointer);
}

// 解析OBJ文件
function loadFaceFile(text) {
  const vertices = [];
  const vt = [];
  const faces = [];
  const lines = text.split("\n");

  lines.forEach((line) => {
    const parts = line.trim().split(/\s+/);
    if (parts[0] === "v") {
      vertices.push(
        parseFloat(parts[1]),
        parseFloat(parts[2]),
        parseFloat(parts[3]),
        parseFloat(parts[4]),
        parseFloat(parts[5]),
      );
    } else if (parts[0] === "f") {
      const face = parts.slice(1).map((part) => {
        const indices = part.split("/").map((index) => parseInt(index, 10) - 1);
        return indices[0];
      });
      faces.push(...face);
    }
  });

  return { vertices, faces };
}

async function loadData() {
  try {
    // 从服务器加载 Gzip 压缩的 JSON 文件
    const response = await fetch("/assets/data.json.gz");
    if (!response.ok) {
      throw new Error("Network response was not ok " + response.statusText);
    }
    console.log("loaddata");
    let combinedData;

    if (import.meta.env.MODE == "development") {
      const buffer = await response.arrayBuffer();
      const contentType = response.headers.get("content-type") || "";
      const contentEncoding = response.headers.get("content-encoding");

      let jsonString;
      // 判断是否需要手动解压
      if (
        contentEncoding === "gzip" ||
        contentType.includes("application/gzip")
      ) {
        // 可能是未解压的 gzip 数据（如 Webpack）
        // 但注意：Vite 虽有 content-encoding:gzip，但数据已解压！
        // 所以更可靠的方式是：尝试解压，失败则当作明文
        try {
          jsonString = pako.inflate(new Uint8Array(buffer), { to: "string" });
        } catch (e) {
          // 解压失败 → 当作已解压的明文（Vite 行为）
          jsonString = new TextDecoder().decode(buffer);
        }
      } else {
        // 普通 JSON 或其他
        jsonString = new TextDecoder().decode(buffer);
      }

      combinedData = JSON.parse(jsonString);
    } else {
      {
        // 如果响应头包含 gzip，但浏览器没有自动解压，手动解压
        const compressedData = await response.arrayBuffer();
        const decompressedData = pako.inflate(new Uint8Array(compressedData), {
          to: "string",
        });
        combinedData = JSON.parse(decompressedData);
      }
    }
    //        if (isGzip)

    // -------
    SetModule(Module, combinedData);

    combinedData.authorized = true;
    combinedData.ref_data = "";
    SetModule(Module2, combinedData);

    // 提取 jsonData
    dataSets = combinedData.json_data;

    // 将 dataSets 的内容逆序并加到原列表后面
    dataSets = dataSets.concat(dataSets.slice().reverse());

    // 提取 objData
    objData = loadFaceFile(combinedData.face3D_obj.join("\n"));
  } catch (error) {
    console.error(error);
    throw error;
  }
}

async function init_opengl() {
  // 加载 combined_data.json.gz
  await loadData();
  {
    // WebGL Shaders
    const vertexShaderSource = `#version 300 es
            layout(location = 0) in vec3 a_position;
            layout(location = 1) in vec2 a_texture;
            uniform float bsVec[12];
            uniform mat4 gProjection;
            uniform mat4 gWorld0;
            uniform sampler2D texture_bs;
            uniform vec2 vertBuffer[209];
            out vec2 v_texture;
            out vec2 v_bias;

            vec4 calculateMorphPosition(vec3 position, vec2 textureCoord) {
                vec4 tmp_Position2 = vec4(position, 1.0);
                if (textureCoord.x < 3.0 && textureCoord.x >= 0.0) {
                    vec3 morphSum = vec3(0.0);
                    for (int i = 0; i < 6; i++) {
                        ivec2 coord = ivec2(int(textureCoord.y), i);
                        vec3 morph = texelFetch(texture_bs, coord, 0).xyz * 2.0 - 1.0;
                        morphSum += bsVec[i] * morph;
                    }
                    tmp_Position2.xyz += morphSum;
                }
                else if (textureCoord.x == 4.0) {
                    // lower teeth
                    vec3 morphSum = vec3(0.0, (bsVec[0] + bsVec[1]) / 2.7 + 6.0, 0.0);
                    tmp_Position2.xyz += morphSum;
                }
                return tmp_Position2;
            }

            void main() {
                mat4 gWorld = gWorld0;

                vec4 tmp_Position2 = calculateMorphPosition(a_position, a_texture);
                vec4 tmp_Position = gWorld * tmp_Position2;

                v_bias = vec2(0.0, 0.0);
                if (a_texture.x == -1.0f) {
                    v_bias = vec2(0.0, 0.0);
                }
                else if (a_texture.y < 209.0f) {
                    vec4 vert_new = gProjection * vec4(tmp_Position.x, tmp_Position.y, tmp_Position.z, 1.0);
                    v_bias = vert_new.xy - (vertBuffer[int(a_texture.y)].xy / 128.0 * 2.0 - 1.0);
                }

                if (a_texture.x >= 3.0f) {
                    gl_Position = gProjection * vec4(tmp_Position.x, tmp_Position.y, 500.0, 1.0);
                }
                else {
                    gl_Position = gProjection * vec4(tmp_Position.x, tmp_Position.y, tmp_Position.z, 1.0);
                }

                v_texture = a_texture;
            }
        `;

    const fragmentShaderSource = `#version 300 es
            precision mediump float;
            in mediump vec2 v_texture;
            in mediump vec2 v_bias;
            out vec4 out_color;

            void main() {
                if (v_texture.x == 2.0f) {
                    out_color = vec4(1.0, 0.0, 0.0, 1.0);
                }
                else if (v_texture.x > 2.0f && v_texture.x < 2.1f) {
                    out_color = vec4(0.5f, 0.0, 0.0, 1.0);
                }
                else if (v_texture.x == 3.0f) {
                    out_color = vec4(0.0, 1.0, 0.0, 1.0);
                }
                else if (v_texture.x == 4.0f) {
                    out_color = vec4(0.0, 0.0, 1.0, 1.0);
                }
                else if (v_texture.x > 3.0f && v_texture.x < 4.0f) {
                    out_color = vec4(0.0, 0.0, 0.0, 1.0);
                }
                else {
                    vec2 wrap = (v_bias.xy + 1.0) / 2.0;
                    out_color = vec4(wrap.xy, 0.5, 1.0);
                }
            }
        `;

    gl = canvas_gl.value.getContext("webgl2", { antialias: false });
    // Compile shaders and link program
    const vertexShader = gl.createShader(gl.VERTEX_SHADER);
    gl.shaderSource(vertexShader, vertexShaderSource);
    gl.compileShader(vertexShader);

    const fragmentShader = gl.createShader(gl.FRAGMENT_SHADER);
    gl.shaderSource(fragmentShader, fragmentShaderSource);
    gl.compileShader(fragmentShader);

    program = gl.createProgram();
    gl.attachShader(program, vertexShader);
    gl.attachShader(program, fragmentShader);
    gl.linkProgram(program);
    gl.useProgram(program);

    // Set up vertex data
    const positionLocation = gl.getAttribLocation(program, "a_position");
    const positionBuffer = gl.createBuffer();

    gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array(objData.vertices),
      gl.STATIC_DRAW,
    );
    gl.enableVertexAttribArray(0);
    gl.vertexAttribPointer(0, 3, gl.FLOAT, false, 20, 0);

    gl.enableVertexAttribArray(1);
    gl.vertexAttribPointer(1, 2, gl.FLOAT, false, 20, 12);

    indexBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
    gl.bufferData(
      gl.ELEMENT_ARRAY_BUFFER,
      new Uint16Array(objData.faces),
      gl.STATIC_DRAW,
    );

    texture_bs = gl.createTexture();
    var image = new Image();
    image.onload = function () {
      gl.bindTexture(gl.TEXTURE_2D, texture_bs);
      gl.texImage2D(
        gl.TEXTURE_2D,
        0,
        gl.RGBA,
        gl.RGBA,
        gl.UNSIGNED_BYTE,
        image,
      );
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
      gl.bindTexture(gl.TEXTURE_2D, null);
    };
    image.src = "share/bs.png";
  }
}

function floatTo16BitPCM(samples) {
  const length = samples.length;
  const int16Array = new Int16Array(length);

  for (let i = 0; i < length; i++) {
    const s = Math.max(-1, Math.min(1, samples[i]));
    int16Array[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
  }

  return int16Array;
}

function playWavAudio(arrayBuffer) {
  const view = new Uint8Array(arrayBuffer);
  const arrayBufferPtr = Module._malloc(arrayBuffer.byteLength);
  Module.HEAPU8.set(view, arrayBufferPtr);
  Module._setAudioBuffer(arrayBufferPtr, arrayBuffer.byteLength);
  Module._free(arrayBufferPtr);
}

function base64ToArrayBuffer(base64, isSlice) {
  // 1. 使用 atob 将 Base64 解码为二进制字符串
  const binaryString = atob(base64);

  // 2. 创建一个 Uint8Array 来存储字节数据
  const length = binaryString.length;
  let bytes = new Uint8Array(length);

  // 3. 将二进制字符串的每个字符转换为字节
  for (let i = 0; i < length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  if (isSlice) {
    // 根据RIFF格式去掉文件头（例如WAV文件通常是44字节）
    let offset = 44; // 对于WAV文件
    if (bytes.length > offset) {
      bytes = bytes.slice(offset);
    } else {
      console.error("Buffer too short to remove header.");
    }
  }

  // 4. 返回 ArrayBuffer
  return bytes.buffer;
}
function removeRiffHeader(buffer) {
  // 直接跳过RIFF头和接下来的8字节（文件大小）
  console.log(buffer.slice(12));

  return buffer.slice(12); // 从第12字节开始，即"WAVE"之后。
}
function parseWavHeader(buffer) {
  const header = {
    chunkId: String.fromCharCode.apply(null, buffer.subarray(0, 4)), // "RIFF"
    chunkSize: buffer.subarray(4, 8), // 文件大小 - 8
    format: String.fromCharCode.apply(null, buffer.subarray(8, 12)), // "WAVE"
    subchunk1Id: String.fromCharCode.apply(null, buffer.subarray(12, 16)), // "fmt "
    subchunk1Size: buffer.subarray(16, 20), // 格式大小
    audioFormat: buffer.subarray(20, 22), // 音频格式，1为PCM
    numChannels: buffer.subarray(22, 24), // 声道数
    sampleRate: buffer.subarray(24, 28), // 采样率
    byteRate: buffer.subarray(28, 32), // 字节率
    blockAlign: buffer.subarray(32, 34), // 数据块对齐单位
    bitsPerSample: buffer.subarray(34, 36), // 位深度
  };
  return header;
}

// 创建BufferSource节点
let source = null;

function PlayWav() {
  if (isStop) return;
  if (props.hideHuman) return;
  if (isPlaying) return;

  isPlaying = true;

  const audioUint8Array = audioQueue.shift(); //从音频队列中取出第一个元素

  playWavAudio(audioUint8Array); //播放音频

  audioContext.decodeAudioData(audioUint8Array, function (audioBuffer) {
    //解码音频数据
    source = audioContext.createBufferSource();
    source.buffer = audioBuffer;
    const gainNode = audioContext.createGain();
    gainNode.gain.value = 0; // 设置初始音量值
    source.connect(gainNode);
    gainNode.connect(audioContext.destination);

    // 连接到输出并播放
    // source.connect(audioContext.destination);
    source.start(0);

    // 当音频播放结束时释放资源
    source.onended = PlayEnd;
  });
}
// 监听播放完成事件
async function PlayEnd() {
  isPlaying = false;
  if (audioQueue.length > 0) {
    PlayWav();
  } else {
    await new Promise((resolve) => setTimeout(resolve, 500));
    IsRecogition = currentInputModelIsVoice;
  }
}

let heartbeatInterval = null;
function startHeartbeat() {
  if (heartbeatInterval || !socket || socket.readyState !== WebSocket.OPEN)
    return;
  heartbeatInterval = setInterval(() => {
    if (socket.readyState === WebSocket.OPEN) {
      try {
        socket.send(JSON.stringify({ action: "ping" }));
        socketVoice.send(JSON.stringify({ action: "ping" }));
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
let mediaRecorder;
let audioChunks = [];

async function StartMicrophone() {
  if (microphoneStream == null) {
    navigator.mediaDevices
      .getUserMedia({
        audio: true,
      })
      .then(async (stream) => {
        microphoneStream = stream;

        const microphoneSource =
          audioContext.createMediaStreamSource(microphoneStream);

        // const scriptProcessor = audioContext.createScriptProcessor(4096, 1, 1); //创建一个脚本处理器，用于处理音频数据

        const biquadFilter = audioContext.createBiquadFilter(); //创建一个 低通滤波器（只保留低于 8000Hz 的频率）去除高频噪音，简化后续处理
        biquadFilter.type = "lowpass";
        biquadFilter.frequency.setValueAtTime(8000, audioContext.currentTime); // Set cut-off frequency to 8kHz

        microphoneSource.connect(biquadFilter); //麦克风 -> 滤波器 -> 脚本处理器
        biquadFilter.connect(scriptProcessor);

        scriptProcessor.onaudioprocess = (event) => {
          if (!IsRecogition) return;
          else if (
            socketVoice == null ||
            socketVoice.readyState != WebSocket.OPEN
          )
            return;

          let audioData = event.inputBuffer.getChannelData(0); //获取第一个声道的浮点音频数据（-1 到 1 之间）
          const sampleRateRatio = audioContext.sampleRate / 16000; //浏览器一般录音是 44.1kHz 或 48kHz，很多语音识别模型要求 16kHz
          const newLength = Math.round(audioData.length / sampleRateRatio); //重新采样到 16kHz
          const resampledData = new Float32Array(newLength);

          for (let i = 0; i < newLength; i++) {
            resampledData[i] = audioData[Math.round(i * sampleRateRatio)];
          }

          socketVoice.send(floatTo16BitPCM(resampledData)); //使用一个函数 floatTo16BitPCM() 把 Float32Array 数据转为 16位PCM格式的二进制数据（很多语音识别 API 的要求）然后通过 socket 发送出去
        };

        scriptProcessor.connect(audioContext.destination);
      })
      .catch((error) => {
        console.error("error:", error);
        ElMessage({
          message: "无法访问麦克风",
          type: "primary",
        });
        emit("micFail");
      });
  }
}

function switchInputMode(isVoice) {
  IsRecogition = currentInputModelIsVoice = isVoice;

  // VoiceInputDiv.classList.remove("hide-with-animation");
  // VoiceInputDiv.classList.remove("show-with-animation");

  // TextInputDiv.classList.remove("hide-with-animation");
  // TextInputDiv.classList.remove("show-with-animation");
  if (IsRecogition) {
    StartMicrophone();

    // VoiceInputDiv.style.display = "flex";

    // VoiceInputDiv.style.pointerEvents = "auto";
    // VoiceInputDiv.classList.add("show-with-animation");

    // TextInputDiv.classList.add("hide-with-animation");
  } else {
    // TextInputDiv.style.display = "flex";
    // TextInputDiv.classList.add("show-with-animation");
    // VoiceInputDiv.style.pointerEvents = "none";
    // VoiceInputDiv.classList.add("hide-with-animation");
  }
}

const stopAudio = () => {
  isStop = true;

  if (player) {
    player.destroy();
    player = null;
  }

  if (socket) {
    socket.send(JSON.stringify({ action: "stop" }));
    console.log("send stop");
  }
  clearAudioQueue();
};

// 隐藏数字人
const hideAudio = () => {
  audioQueue = [];

  if (source) {
    source.stop();
    source.disconnect(); // 停止播放音频源节点（在某些浏览器中可能需要使用source.disconnect()）
    source = null; // 重置source引用，以便可以重新播放相同的音频文件而不需要重新加载它。
  }

  clearAudioQueue();
  if (player) {
    player.destroy();
    player = null;
  }
};

const stopMic = () => {
  socketVoice.send(JSON.stringify({ action: "ars_finish" }));
  scriptProcessor.disconnect();
  microphoneStream.getTracks().forEach((track) => track.stop());
  microphoneStream = null;
  switchInputMode(false);
};

async function Initialized() {
  videoRef.value.play();
  // switchInputMode(true);
  while (true) {
    await new Promise((resolve) => setTimeout(resolve, 500));
    if (!videoRef.value.paused) {
      processV();
      break;
    }
    console.log("未播放");
  }
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = src;
    script.onload = () => resolve();
    script.onerror = (e) => reject(e);
    document.body.appendChild(script);
  });
}

function render_image(mat_world, subPoints, bsArray) {
  gl.useProgram(program);
  const worldMatUniformLocation = gl.getUniformLocation(program, "gWorld0");
  gl.uniformMatrix4fv(worldMatUniformLocation, false, mat_world);

  gl.uniform2fv(gl.getUniformLocation(program, "vertBuffer"), subPoints);
  gl.uniform1fv(gl.getUniformLocation(program, "bsVec"), bsArray);

  const projectionUniformLocation = gl.getUniformLocation(
    program,
    "gProjection",
  );
  const orthoMatrix = mat4.create();
  mat4.ortho(orthoMatrix, 0, 128, 0, 128, 1000, -1000);
  gl.uniformMatrix4fv(projectionUniformLocation, false, orthoMatrix);

  gl.enable(gl.DEPTH_TEST);
  gl.enable(gl.BLEND);
  gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
  gl.enable(gl.CULL_FACE);
  gl.cullFace(gl.BACK);
  gl.frontFace(gl.CW);
  gl.clearColor(0.5, 0.5, 0.5, 0);
  gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

  gl.activeTexture(gl.TEXTURE0);
  gl.bindTexture(gl.TEXTURE_2D, texture_bs);
  gl.uniform1i(gl.getUniformLocation(program, "texture_bs"), 0);

  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
  gl.bindFramebuffer(gl.FRAMEBUFFER, null);

  const width = gl.drawingBufferWidth;
  const height = gl.drawingBufferHeight;
  gl.drawElements(gl.TRIANGLES, objData.faces.length, gl.UNSIGNED_SHORT, 0);

  gl.readPixels(0, 0, width, height, gl.RGBA, gl.UNSIGNED_BYTE, pixels_fbo);
}

async function processV() {
  canvas_video.value.width = videoRef.value.videoWidth;
  canvas_video.value.height = videoRef.value.videoHeight;
  let lastDataSetIndex = -1; // 初始化为一个不可能的索引值
  let isProcessing = false; // 标志位

  let lastVideoTime = 0; // 初始化为一个不可能的索引值

  const frameCallback = async (currentTime) => {
    if (
      videoRef?.value &&
      !videoRef.value.paused &&
      !videoRef.value.ended &&
      !isProcessing
    ) {
      isProcessing = true;

      try {
        // 计算当前数据集索引
        const currentDataSetIndex = Math.floor(videoRef.value.currentTime * 25);
        lastVideoTime = videoRef.value.currentTime;
        if (
          lastDataSetIndex !== currentDataSetIndex &&
          currentDataSetIndex < dataSets.length - 1
        ) {
          lastDataSetIndex = currentDataSetIndex;
          // 清除画布并绘制当前视频帧到canvas
          ctx_video.clearRect(
            0,
            0,
            canvas_video.value.width,
            canvas_video.value.height,
          );
          ctx_video.drawImage(
            videoRef.value,
            0,
            0,
            canvas_video.value.width,
            canvas_video.value.height,
          );
          // 处理当前数据集
          if (currentDataSetIndex < dataSets.length - 1) {
            // 创建一个ArrayBuffer来存储浮点数
            const floatArraySize = 12;
            const floatArrayBytes = floatArraySize * 4; // 每个浮点数占用4个字节

            var bsPtr = allocateMemory(floatArrayBytes);
            Module._updateBlendShape(bsPtr, floatArrayBytes);
            // 从Wasm内存中取出数据
            var bsArray = new Float32Array(
              Module.HEAPU8.buffer,
              bsPtr,
              floatArraySize,
            );

            const dataSet = dataSets[currentDataSetIndex];
            const rect = dataSet.rect;

            const currentTimeStamp = 0.04 * currentDataSetIndex;
            const nextTimeStamp = 0.04 * (currentDataSetIndex + 1);
            const currentpoints = dataSets[currentDataSetIndex].points;
            const nextpoints = dataSets[currentDataSetIndex + 1].points;

            // 线性插值计算
            const t =
              (videoRef.value.currentTime - currentTimeStamp) /
              (nextTimeStamp - currentTimeStamp);
            let points = currentpoints.map(
              (xi, index) => (1 - t) * xi + t * nextpoints[index],
            );
            // 创建一个新的 mat4 对象
            let matrix = mat4.create();

            mat4.set(
              matrix,
              points[0],
              points[1],
              points[2],
              points[3],
              points[4],
              points[5],
              points[6],
              points[7],
              points[8],
              points[9],
              points[10],
              points[11],
              points[12],
              points[13],
              points[14],
              points[15],
            );
            const subPoints = points.slice(16);
            render_image(matrix, subPoints, bsArray);

            // 创建临时画布用于裁剪、缩放和绘点
            const tempCanvas = document.createElement("canvas");
            const tempCtx = tempCanvas.getContext("2d");

            // 获取rect区域图像数据并绘制到临时画布
            tempCanvas.width = rect[2] - rect[0];
            tempCanvas.height = rect[3] - rect[1];
            tempCtx.drawImage(
              videoRef.value,
              rect[0],
              rect[1],
              rect[2] - rect[0],
              rect[3] - rect[1],
              0,
              0,
              tempCanvas.width,
              tempCanvas.height,
            );

            // 缩放到128x128
            resizedCanvas.width = 128;
            resizedCanvas.height = 128;
            resizedCtx.drawImage(tempCanvas, 0, 0, 128, 128);

            // 获取128x128图像数据并处理
            const imageData = resizedCtx.getImageData(0, 0, 128, 128);

            var data = imageData.data;

            const imageDataPtr = allocateMemory(imageData.data.length);
            Module.HEAPU8.set(imageData.data, imageDataPtr);

            const imageDataPtr2 = allocateMemory2(imageData.data.length);
            Module2.HEAPU8.set(imageData.data, imageDataPtr2);

            const imageDataGlPtr = allocateMemory(pixels_fbo.length);
            Module.HEAPU8.set(pixels_fbo, imageDataGlPtr);

            const imageDataGlPtr2 = allocateMemory2(pixels_fbo.length);
            Module2.HEAPU8.set(pixels_fbo, imageDataGlPtr2);

            Module._processImage(
              imageDataPtr,
              128,
              128,
              imageDataGlPtr,
              128,
              128,
            );
            const result = Module.HEAPU8.subarray(
              imageDataPtr,
              imageDataPtr + imageData.data.length,
            );

            Module2._processImage(
              imageDataPtr2,
              128,
              128,
              imageDataGlPtr2,
              128,
              128,
            );
            const result2 = Module2.HEAPU8.subarray(
              imageDataPtr2,
              imageDataPtr2 + imageData.data.length,
            );

            let logoSize = 40;
            const startX = 128 - logoSize; // 右下角 x 坐标
            const startY = 128 - logoSize; // 右下角 y 坐标

            for (let y = 0; y < logoSize; y++) {
              for (let x = 0; x < logoSize; x++) {
                const srcIndex = ((startY + y) * 128 + (startX + x)) * 4;
                result[srcIndex] = result2[srcIndex]; // R
                result[srcIndex + 1] = result2[srcIndex + 1]; //G
                result[srcIndex + 2] = result2[srcIndex + 2]; //B
                result[srcIndex + 3] = result2[srcIndex + 3]; //A
              }
            }

            imageData.data.set(result);

            // 更新 canvas 上的图像显示
            resizedCtx.putImageData(imageData, 0, 0);
            // 恢复图像到原始尺寸
            tempCtx.clearRect(0, 0, tempCanvas.width, tempCanvas.height);
            tempCtx.drawImage(
              resizedCanvas,
              0,
              0,
              tempCanvas.width,
              tempCanvas.height,
            );

            // 将临时画布的内容放回原画布
            ctx_video.drawImage(tempCanvas, rect[0], rect[1]);

            freeMemory(imageDataPtr);
            freeMemory2(imageDataPtr2);
            freeMemory(imageDataGlPtr);
            freeMemory2(imageDataGlPtr2);
            freeMemory(bsPtr);
          }
        }

        isProcessing = false; // 处理完成后将标志位置为false
      } catch (error) {
        console.error("Error processing frame:", error);
        isProcessing = false; // 即使出错也要将标志位置为false
      }

      requestAnimationFrame(frameCallback);
    }
  };

  requestAnimationFrame(frameCallback);
}

function allocateMemory(size) {
  const ptr = Module._malloc(size);
  if (ptr === 0) throw new Error("Failed to allocate memory");
  return ptr;
}

function freeMemory(ptr) {
  if (ptr !== null && ptr !== 0) {
    Module._free(ptr);
  }
}

function allocateMemory2(size) {
  const ptr = Module2._malloc(size);
  if (ptr === 0) throw new Error("Failed to allocate memory");
  return ptr;
}

function freeMemory2(ptr) {
  if (ptr !== null && ptr !== 0) {
    Module2._free(ptr);
  }
}

onMounted(async () => {
  // await loadScript("https://cdn.jsdelivr.net/npm/pako@2.1.0/dist/pako.min.js");

  const response = await fetch("/assets/01.mp4");
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  videoRef.value.src = url;

  await videoRef.value.play();
  await Promise.all([
    loadScript("/js_source/loadMode1.js"),
    loadScript("/js_source/loadMode2.js"),
  ]);

  await init();
  if (!getToken()) {
    return;
  }
  socketVoice = new WebSocket(
    `${import.meta.env.VITE_APP_WEBSOCKET_BASE_URL_VOICE}`,
    getToken(),
  );
  socketVoice.addEventListener("open", (event) => {
    console.log("socketVoice open");
  });
  socketVoice.addEventListener("message", (event) => {
    const jsonData = JSON.parse(event.data);

    // 语音
    if (jsonData.type == "voice") {
      if (isStop || props.hideHuman) {
        return;
      }
      if (jsonData.last == "true") {
        player.destroy();
        player = null;
        return;
      }
      const data = jsonData.content;

      if (!/^\s*$/.test(data)) {
        const audioUint8Array = base64ToArrayBuffer(data, false);

        if (!player) {
          player = new PCMPlayer({
            encoding: "16bitInt", // 采样位数
            channels: 1, // 通道
            sampleRate: 16000, // 采样率
            flushingTime: 2000, // pcm刷新间隔
            onstatechange: (node, event, type) => {
              console.log("onstatechange");
            }, // 播放状态变化事件
            onended: (node, event) => {
              console.log("onended");

              // isPlaying = false;
              // if (audioQueue.length > 0) {
              //   PlayWav();
              // }
            }, // 播放结束事件
          });
        }
        audioQueue.push(audioUint8Array);
        PlayWav();
        const wav = base64ToArrayBuffer(data, true);
        // 获取播放器实例后
        player.gainNode.gain.setValueAtTime(1.2, player.audioCtx.currentTime);
        player.feed(wav);
      }
    }
  });
  socket = new WebSocket(
    `${import.meta.env.VITE_APP_WEBSOCKET_BASE_URL}`,
    getToken(),
  );

  socket.addEventListener("open", (event) => {
    emit("ready");
    startHeartbeat();
  });
  socket.addEventListener("message", (event) => {
    try {
      if (isStop) {
        return;
      }
      // 解析接收到的 JSON 数据
      const jsonData = JSON.parse(event.data);
      if (jsonData.type == "text") {
        // 回答
        emit("answer", jsonData);
      } else if (jsonData.type == "ars") {
        // 语音转文字
        console.log(jsonData);

        emit("translate", jsonData);
      } else if (jsonData.type == "question") {
        // 考核返回问题
        console.log(jsonData);

        emit("testQuestion", jsonData);
      } else if (jsonData.type == "last-question") {
        // 考核结束
        console.log(jsonData);

        emit("testQuestionLast", jsonData);
      } else if (jsonData.type == "assess-final") {
        // 报错，考完
        console.log(jsonData);
        emit("testQuestionFinish", jsonData);
      } else if (jsonData.type == "StartLLM") {
        const data = jsonData.Data;
        IsRecogition = false;

        audioQueue = [];

        if (!audioContext || audioContext.state === "closed") {
          audioContext = new (window.AudioContext || window.webkitAudioContext)(
            {
              latencyHint: "interactive",
            },
          );
        } else if (audioContext.state === "suspended") {
          audioContext.resume(); // 如果处于暂停状态，则恢复
        }
      }
    } catch (error) {
      console.error("解析 JSON 失败:", error);
    }
  });
});

onUnmounted(() => {
  if (player) {
    player.destroy();
  }

  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.close();
  }

  if (socketVoice && socketVoice.readyState === WebSocket.OPEN) {
    socketVoice.close();
  }
  stopHeartbeat();
});

// 考核提问
const getQuestionGeneration = (params, isLast) => {
  if (socket && socket.readyState === WebSocket.OPEN) {
    let requestMessage = {
      action: "generate_question", // 自定义动作，告诉后端考核提问
      text: params, // 节点ID
    };
    if (isLast == "true") {
      requestMessage = {
        action: "generate_question_final",
        text: params, // 课程ID
      };
    }
    isStop = false;
    socket.send(JSON.stringify(requestMessage));
  } else {
    console.warn(
      "DH_live: WebSocket is not open. Cannot send voice generation request.",
    );
  }
};

const sendTextForVoiceGeneration = (text, nodeId) => {
  if (socket && socket.readyState === WebSocket.OPEN) {
    const requestMessage = {
      action: "generate_voice", // 自定义动作，告诉后端这是一个语音生成请求
      text: text, // 要生成语音的文本
      id: nodeId || "",
      // 可以添加其他参数，例如语音类型、说话人等
    };
    isStop = false;
    socket.send(JSON.stringify(requestMessage));
  } else {
    console.warn(
      "DH_live: WebSocket is not open. Cannot send voice generation request.",
    );
  }
};
const clearAudioQueue = () => {
  if (typeof window.audioQueue !== "undefined") {
    window.audioQueue = [];
    console.log("DH_live: Audio queue cleared.");
  }
};

// 辅助函数：将二维数组扁平化为一维数组
function flattenArray(arrays) {
  const buffer = new ArrayBuffer(arrays.length * 4096 * 2);
  const view = new DataView(buffer);
  let offset = 0;

  for (var i = 0; i < arrays.length; i++) {
    for (var j = 0; j < arrays[i].length; j++) {
      data = parseInt(arrays[i][j] * 32768);
      view.setUint16(offset, data, true);
      offset += 2;
    }
  }
  return buffer.slice(0, offset);
}

// 辅助函数：创建WAV文件头
function createWavHeader(dataSize, sampleRate) {
  const buffer = new ArrayBuffer(44);
  const view = new DataView(buffer);

  // Chunk ID
  view.setUint32(0, 0x52494646, false); // "RIFF"

  // File size (excluding first 8 bytes)
  console.log(dataSize);
  view.setUint32(4, dataSize + 36, true);

  // Format (WAVE)
  view.setUint32(8, 0x57415645, false); // "WAVE"

  // Subchunk 1 ID (fmt)
  view.setUint32(12, 0x666d7420, false); // "fmt "

  // Subchunk 1 size
  view.setUint32(16, 16, true);

  // Audio format (PCM)
  view.setUint16(20, 1, true);

  // Number of channels (1 for mono)
  view.setUint16(22, 1, true);

  // Sample rate
  view.setUint32(24, sampleRate, true);

  // Byte rate (sample rate * block align)
  view.setUint32(28, sampleRate * 2, true);

  // Block align (number of bytes per sample)
  view.setUint16(32, 2, true);

  // Bits per sample
  view.setUint16(34, 16, true);

  // Subchunk 2 ID (data)
  view.setUint32(36, 0x64617461, false); // "data"

  // Subchunk 2 size
  view.setUint32(40, dataSize, true);

  return buffer;
}

defineExpose({
  sendTextForVoiceGeneration, // 暴露发送请求的方法
  stopAudio, // 暴露清空队列的方法
  switchInputMode, //语音输入
  stopMic, //关闭麦克风
  getQuestionGeneration, //考核问题
  hideAudio,
});
</script>

<style scoped>
body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  margin: 0;
  background-color: #0a0a0a;
  overflow: hidden;
}

/* video,
canvas {
  border: 2px solid #ccc;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
} */

#video {
  position: absolute;
  top: -9999px;
  left: -9999px;
  width: 1px;
  height: 1px;
  object-fit: contain;
}
/* 隐藏视频元素的画中画按钮 */
video::-webkit-media-controls-picture-in-picture-button {
  display: none;
}

/* #canvas_video {
  width: auto;
  height: 50vh;
} */

#canvas_gl {
  position: absolute;
  top: -9999px;
  left: -9999px;
  width: 128px;
  height: 128px;
}

#screen {
  position: absolute;
  bottom: -1000px;
  /* 注意：这个值会使元素在屏幕外很远 */
  right: -1000px;
  width: 1px;
  height: 1px;
  display: none;
}
</style>
