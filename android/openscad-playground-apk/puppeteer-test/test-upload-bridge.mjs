// Verifies the "Upload file(s)" menu command + native open-file bridge
// round trip: clicking the menu item calls window.AndroidFileBridge.openFile()
// (mocked here, since the real Android picker can't run headlessly), which
// the mock immediately "answers" by invoking window.__openScadNativeOpenFile
// with base64 content + filename -- exactly what MainActivity.onActivityResult
// does on-device -- and we confirm the editor switches to that file with the
// right content.
import puppeteer from 'puppeteer-core';
import fs from 'node:fs';

const CHROME_PATH = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';
const URL = 'http://localhost:8123/index.html';
const ANDROID_UA = 'Mozilla/5.0 (Linux; Android 16; Redmi Note 14 Pro 5G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.0.0 Mobile Safari/537.36';
// Extracted verbatim from the compiled AssetWebViewClient (see the
// extraction step in test-download-bridge.mjs / this session's history):
// this is exactly the script the native layer injects into index.html,
// including window.__openScadNativeOpenFile.
const INTERCEPT_JS = fs.readFileSync('/tmp/extracted3.js', 'utf8');

const UPLOADED_SCAD = `// uploaded from native picker\ncylinder(h=5, r=3);`;

(async () => {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 420, height: 900 });
  await page.setUserAgent(ANDROID_UA);

  const logs = [];
  page.on('console', (msg) => { const l = `[console.${msg.type()}] ${msg.text()}`; logs.push(l); console.log(l); });
  page.on('pageerror', (err) => { const l = `[pageerror] ${err.message}`; logs.push(l); console.log(l); });

  // Mock the native bridge: openFile() immediately "returns" by calling the
  // same global function MainActivity.onActivityResult calls on-device.
  await page.evaluateOnNewDocument((content) => {
    window.AndroidFileBridge = {
      saveFile: () => {},
      openFile: () => {
        const bytes = new TextEncoder().encode(content);
        let binary = '';
        for (const b of bytes) binary += String.fromCharCode(b);
        const base64 = btoa(binary);
        setTimeout(() => window.__openScadNativeOpenFile(base64, 'test-upload.scad'), 50);
      },
    };
  }, UPLOADED_SCAD);
  await page.evaluateOnNewDocument(INTERCEPT_JS);

  await page.goto(URL, { waitUntil: 'networkidle2', timeout: 60000 });
  await page.waitForSelector('textarea.openscad-editor', { timeout: 60000 });
  await new Promise(r => setTimeout(r, 1500));

  // Auto-preview switches to the "View" tab; go back to "Edit" for the menu.
  const editTab = await page.evaluateHandle(() => Array.from(document.querySelectorAll('a, button')).find(el => el.textContent.trim() === 'Edit'));
  if (editTab) { await editTab.asElement().click(); }
  await new Promise(r => setTimeout(r, 500));

  const menuBtn = await page.waitForSelector('button[title="Editor menu"]', { timeout: 15000 });
  await menuBtn.click();
  await new Promise(r => setTimeout(r, 500));

  const clicked = await page.evaluate(() => {
    const items = Array.from(document.querySelectorAll('.p-menuitem-text'));
    const item = items.find(el => el.textContent.trim() === 'Upload file(s)');
    if (!item) return 'NOT_FOUND';
    const li = item.closest('li');
    if (li && li.className.includes('p-disabled')) return 'DISABLED';
    item.closest('a, li').click();
    return 'CLICKED';
  });
  console.log('upload menu click result:', clicked);

  await new Promise(r => setTimeout(r, 1500));

  const result = await page.evaluate(() => {
    const ta = document.querySelector('textarea.openscad-editor');
    return {
      textareaValue: ta ? ta.value : null,
      activePath: window.__openScadModel ? window.__openScadModel.state.params.activePath : null,
    };
  });
  console.log('result:', JSON.stringify(result));

  const pass = clicked === 'CLICKED'
    && result.activePath === '/test-upload.scad'
    && result.textareaValue && result.textareaValue.includes('cylinder(h=5, r=3)');
  console.log('--- RESULT:', pass ? 'PASS' : 'FAIL', '---');

  await browser.close();
  process.exit(0);
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
