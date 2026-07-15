export const fitScreen = (name) => {
  const body = document.documentElement;
  const bodyWidth = body.clientWidth;
  const bodyHeight = body.clientHeight;
  const realRatio = bodyWidth / bodyHeight;
  const designRatio = 32 / 10;
  const scaleRate =
    realRatio > designRatio ? bodyHeight / 1080 : bodyWidth / 1920;

  const app = document.querySelector(name);
  // app && (app.style.transformOrigin = 'left top') && (app.style.transform = `scale(${scaleRate}) translateX(-50%)`) && (app.style.width = `${bodyWidth / scaleRate}px`)

  app &&
    (app.style.transformOrigin = "left top") &&
    (app.style.transform = `scale(${scaleRate})`) &&
    (app.style.width = `${bodyWidth / scaleRate}px`);
};

export const fitScreenHeight = (name) => {
  const app = document.querySelector(name);
  app &&
    (app.style.transformOrigin = "left top") &&
    (app.style.transform = `scaleY(${app?.clientHeight / 1200})`);
};
