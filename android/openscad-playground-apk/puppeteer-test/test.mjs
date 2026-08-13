import puppeteer from 'puppeteer-core';

const CHROME_PATH = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';
const URL = 'http://localhost:8123/index.html';

const ANDROID_UA = 'Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/119.0.0.0 Mobile Safari/537.36';

const SAMPLE_SCAD = `
difference() {
  cube([20,20,20], center=true);
  sphere(r=12);
}
`;

const mode = process.argv[2] || 'desktop'; // 'desktop' or 'android'

(async () => {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--use-gl=angle', '--use-angle=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 420, height: 900 });

  if (mode === 'android') {
    await page.setUserAgent(ANDROID_UA);
    console.log('Using Android WebView user-agent (Monaco disabled -> textarea fallback path, same as real APK).');
  } else {
    console.log('Using default desktop Chromium user-agent (Monaco editor path).');
  }

  const logs = [];
  page.on('console', (msg) => {
    const line = `[console.${msg.type()}] ${msg.text()}`;
    logs.push(line);
    console.log(line);
  });
  page.on('pageerror', (err) => {
    const line = `[pageerror] ${err.message}`;
    logs.push(line);
    console.log(line);
  });
  page.on('requestfailed', (req) => {
    const line = `[requestfailed] ${req.url()} ${req.failure()?.errorText}`;
    logs.push(line);
    console.log(line);
  });

  console.log('Navigating to', URL);
  await page.goto(URL, { waitUntil: 'networkidle2', timeout: 60000 });

  let setOk = false;
  if (mode === 'android') {
    console.log('Waiting for textarea fallback editor...');
    await page.waitForSelector('textarea.openscad-editor', { timeout: 60000 });
    await new Promise(r => setTimeout(r, 1500));
    await page.click('textarea.openscad-editor');
    await page.keyboard.down('Control');
    await page.keyboard.press('KeyA');
    await page.keyboard.up('Control');
    await page.keyboard.type(SAMPLE_SCAD, { delay: 3 });
    setOk = true;
  } else {
    console.log('Waiting for Monaco editor...');
    await page.waitForSelector('.monaco-editor', { timeout: 60000 });
    await new Promise(r => setTimeout(r, 3000));

    console.log('Setting editor content via Monaco API...');
    setOk = await page.evaluate((code) => {
      const w = window;
      if (w.monaco && w.monaco.editor) {
        const models = w.monaco.editor.getModels();
        if (models && models.length > 0) {
          models[0].setValue(code);
          return true;
        }
      }
      return false;
    }, SAMPLE_SCAD);
    console.log('Monaco setValue via API:', setOk);

    if (!setOk) {
      console.log('Falling back to click+type');
      await page.click('.monaco-editor');
      await page.keyboard.down('Control');
      await page.keyboard.press('KeyA');
      await page.keyboard.up('Control');
      await page.keyboard.type(SAMPLE_SCAD, { delay: 5 });
    }
  }

  await new Promise(r => setTimeout(r, 1000));

  console.log('Pressing F5 (preview)...');
  await page.keyboard.press('F5');
  await new Promise(r => setTimeout(r, 8000));

  console.log('Pressing F6 (full render)...');
  await page.keyboard.press('F6');

  const start = Date.now();
  let renderDone = false;
  while (Date.now() - start < 120000) {
    await new Promise(r => setTimeout(r, 2000));
    if (logs.some(l => /Total rendering time/i.test(l))) {
      renderDone = true;
      break;
    }
  }
  console.log('Render finished (heuristic):', renderDone);

  await new Promise(r => setTimeout(r, 3000));

  const shotPath = `/home/user/image-to-stl/android/openscad-playground-apk/puppeteer-test/screenshot-${mode}.png`;
  await page.screenshot({ path: shotPath });
  console.log('Screenshot saved to', shotPath);

  const errorLogs = logs.filter(l => /\[pageerror\]|\[requestfailed\]|console\.error/i.test(l));
  console.log('--- ERROR-LEVEL LOGS ---');
  errorLogs.forEach(l => console.log(l));
  console.log('--- TOTAL LOG LINES:', logs.length, '---');
  console.log('--- RESULT:', renderDone && errorLogs.length === 0 ? 'PASS' : (renderDone ? 'PASS_WITH_WARNINGS' : 'FAIL'), '---');

  await browser.close();
  process.exit(0);
})().catch((e) => {
  console.error('FATAL', e);
  process.exit(1);
});
