import mitt from "@/utils/mitt";

const emitter = mitt();

export function mittEmit(name, event) {
  emitter.emit(name, event);
}

export function mittOn(name, event) {
  emitter.on(name, event);
}

export function mittOff(name, event) {
  emitter.off(name, event);
}

export function mittClear() {
  emitter.clear();
}
