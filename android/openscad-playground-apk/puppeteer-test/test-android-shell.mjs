// Simulates exactly what AssetWebViewClient.shouldInterceptRequest does on
// Android: serve assets/www/* under a virtual https://appassets.androidplatform.net/
// origin instead of file://, via request interception (the closest thing to
// "run the real WebView glue" available without an Android device/emulator).
import puppeteer from 'puppeteer-core';
import fs from 'node:fs';
import path from 'node:path';

const CHROME_PATH = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';
// AssetManager.open("www/index.html") resolves relative to the APK's
// top-level assets/ folder, so mirror that root here (not assets/www/) --
// the URL path already carries the "www/" prefix, matching MainActivity's
// real "https://appassets.androidplatform.net/www/index.html".
const ASSETS_DIR = '/home/user/image-to-stl/android/openscad-playground-apk/app/assets';
const VIRTUAL_ORIGIN = 'https://appassets.androidplatform.net';
const ANDROID_UA = 'Mozilla/5.0 (Linux; Android 16; Redmi Note 14 Pro 5G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.0.0 Mobile Safari/537.36';

const MIME = {
  '.html': 'text/html', '.htm': 'text/html', '.js': 'application/javascript',
  '.css': 'text/css', '.json': 'application/json', '.wasm': 'application/wasm',
  '.woff2': 'font/woff2', '.woff': 'font/woff', '.ttf': 'font/ttf',
  '.eot': 'application/vnd.ms-fontobject', '.svg': 'image/svg+xml',
  '.png': 'image/png', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg',
  '.ico': 'image/x-icon', '.glb': 'model/gltf-binary', '.zip': 'application/zip',
  '.wav': 'audio/wav', '.txt': 'text/plain',
};
function guessMime(p) {
  const ext = path.extname(p).toLowerCase();
  return MIME[ext] || 'application/octet-stream';
}

const SAMPLE_SCAD = `
difference() {
  cube([20,20,20], center=true);
  sphere(r=12);
}
`;

(async () => {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--use-gl=angle', '--use-angle=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 420, height: 900 });
  await page.setUserAgent(ANDROID_UA);
  await page.setRequestInterception(true);

  page.on('request', (req) => {
    (async () => {
      try {
        const url = new URL(req.url());
        if (url.origin !== VIRTUAL_ORIGIN) {
          return await req.abort();
        }
        const relPath = decodeURIComponent(url.pathname).replace(/^\//, '');
        const filePath = path.join(ASSETS_DIR, relPath);
        if (!filePath.startsWith(ASSETS_DIR) || !fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
          console.log('[404]', req.url(), '->', filePath);
          return await req.respond({ status: 404, body: 'not found' });
        }
        await req.respond({
          status: 200,
          contentType: guessMime(filePath),
          body: fs.readFileSync(filePath),
        });
      } catch (e) {
        console.log('[request-handler-error]', req.url(), e.message);
      }
    })();
  });

  const logs = [];
  page.on('console', (msg) => { const l = `[console.${msg.type()}] ${msg.text()}`; logs.push(l); console.log(l); });
  page.on('pageerror', (err) => { const l = `[pageerror] ${err.message}`; logs.push(l); console.log(l); });

  console.log('Navigating to', `${VIRTUAL_ORIGIN}/www/index.html`);
  await page.goto(`${VIRTUAL_ORIGIN}/www/index.html`, { waitUntil: 'networkidle2', timeout: 60000 });

  console.log('Waiting for textarea fallback editor (Android UA)...');
  await page.waitForSelector('textarea.openscad-editor', { timeout: 60000 });
  await new Promise(r => setTimeout(r, 1500));
  await page.click('textarea.openscad-editor');
  await page.keyboard.down('Control');
  await page.keyboard.press('KeyA');
  await page.keyboard.up('Control');
  await page.keyboard.type(SAMPLE_SCAD, { delay: 3 });

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
    if (logs.some(l => /Total rendering time/i.test(l))) { renderDone = true; break; }
  }
  console.log('Render finished (heuristic):', renderDone);
  await new Promise(r => setTimeout(r, 2000));

  await page.screenshot({ path: '/home/user/image-to-stl/android/openscad-playground-apk/puppeteer-test/screenshot-android-shell.png' });

  const errorLogs = logs.filter(l => /\[pageerror\]|console\.error/i.test(l));
  console.log('--- ERROR-LEVEL LOGS ---');
  errorLogs.forEach(l => console.log(l));
  console.log('--- RESULT:', renderDone && errorLogs.length === 0 ? 'PASS' : (renderDone ? 'PASS_WITH_WARNINGS' : 'FAIL'), '---');

  await browser.close();
  process.exit(0);
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
