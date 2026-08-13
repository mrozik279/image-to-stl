// Validates the download-interception JS (extracted verbatim from
// AssetWebViewClient.smali) actually catches the "Download STL" blob
// download and hands correct data to a mock window.AndroidFileBridge,
// exactly like the real Android bridge would receive it.
import puppeteer from 'puppeteer-core';
import fs from 'node:fs';

const CHROME_PATH = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';
const URL = 'http://localhost:8123/index.html';
const INTERCEPT_JS = fs.readFileSync('/tmp/extracted.js', 'utf8');

const SAMPLE_SCAD = `cube([10,10,10]);`;

(async () => {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--use-gl=angle', '--use-angle=swiftshader', '--enable-webgl', '--ignore-gpu-blocklist', '--enable-unsafe-swiftshader'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 1000 });

  const logs = [];
  page.on('console', (msg) => { const l = `[console.${msg.type()}] ${msg.text()}`; logs.push(l); console.log(l); });
  page.on('pageerror', (err) => { const l = `[pageerror] ${err.message}`; logs.push(l); console.log(l); });

  // Install the mock native bridge + the exact injected script BEFORE any
  // page script runs, same as it would be available from page-load time
  // in the real app (injected server-side into index.html).
  await page.evaluateOnNewDocument(() => {
    window.__savedFiles = [];
    window.AndroidFileBridge = {
      saveFile: (base64, filename, mimeType) => {
        window.__savedFiles.push({ base64, filename, mimeType });
        console.log('BRIDGE_SAVE_CALLED', filename, mimeType, base64.length);
      },
    };
  });
  await page.evaluateOnNewDocument(INTERCEPT_JS);

  console.log('Navigating to', URL);
  await page.goto(URL, { waitUntil: 'networkidle2', timeout: 60000 });
  await page.waitForSelector('.monaco-editor', { timeout: 60000 });
  await new Promise(r => setTimeout(r, 3000));

  await page.evaluate((code) => {
    const models = window.monaco.editor.getModels();
    models[0].setValue(code);
  }, SAMPLE_SCAD);

  await page.click('.monaco-editor');
  await page.keyboard.press('F6'); // full render
  const start = Date.now();
  while (Date.now() - start < 60000) {
    await new Promise(r => setTimeout(r, 1000));
    if (logs.some(l => /Total rendering time/i.test(l))) break;
  }
  await new Promise(r => setTimeout(r, 1500));

  console.log('Clicking Download STL...');
  // ExportButton is a PrimeReact SplitButton whose main area's onClick calls model.export()
  const clicked = await page.evaluate(() => {
    const btn = Array.from(document.querySelectorAll('button')).find(b => /download/i.test(b.textContent || '') || /download/i.test(b.getAttribute('aria-label') || ''));
    // SplitButton renders two buttons (main label button + dropdown toggle); the main one has the label text
    const candidates = Array.from(document.querySelectorAll('.p-splitbutton-defaultbutton, .p-splitbutton .p-button'));
    const target = candidates.find(b => /download/i.test(b.textContent || '')) || btn;
    if (!target) return 'NOT_FOUND';
    target.click();
    return 'CLICKED:' + (target.textContent || '');
  });
  console.log('click result:', clicked);

  await new Promise(r => setTimeout(r, 3000));

  const saved = await page.evaluate(() => window.__savedFiles);
  console.log('--- saved files via bridge ---');
  console.log(JSON.stringify((saved || []).map(s => ({ filename: s.filename, mimeType: s.mimeType, base64Len: s.base64.length })), null, 2));

  let result = 'FAIL';
  if (saved && saved.length > 0) {
    const f = saved[0];
    const bytes = Buffer.from(f.base64, 'base64');
    console.log('Decoded byte length:', bytes.length, 'first bytes (ascii):', bytes.slice(0, 6).toString('ascii'));
    // Binary STL starts with an 80-byte header (often "OpenSCAD Model..." or similar), followed by
    // a 4-byte little-endian triangle count. Just sanity check it's non-trivial in size.
    result = bytes.length > 100 ? 'PASS' : 'FAIL_EMPTY';
  }
  console.log('--- RESULT:', result, '---');

  await browser.close();
  process.exit(0);
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
