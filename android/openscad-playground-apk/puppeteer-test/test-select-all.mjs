// Verifies the "Select All" editor-menu command works on the Android
// fallback path (plain <textarea>, since Monaco is disabled there).
import puppeteer from 'puppeteer-core';

const CHROME_PATH = '/opt/pw-browsers/chromium-1194/chrome-linux/chrome';
const URL = 'http://localhost:8123/index.html';
const ANDROID_UA = 'Mozilla/5.0 (Linux; Android 16; Redmi Note 14 Pro 5G) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/128.0.0.0 Mobile Safari/537.36';

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

  await page.goto(URL, { waitUntil: 'networkidle2', timeout: 60000 });
  await page.waitForSelector('textarea.openscad-editor', { timeout: 60000 });
  await new Promise(r => setTimeout(r, 1500));

  // The app auto-previews the default demo on load and switches to the
  // "View" tab, which covers the editor panel (and its "..." menu button)
  // in this narrow/mobile layout. Switch back to "Edit" first.
  const editTab = await page.evaluateHandle(() => Array.from(document.querySelectorAll('a, button')).find(el => el.textContent.trim() === 'Edit'));
  if (editTab) { await editTab.asElement().click(); }
  await new Promise(r => setTimeout(r, 500));

  // Open the editor "..." menu and click "Select All"
  const menuBtn = await page.waitForSelector('button[title="Editor menu"]', { timeout: 15000 });
  await menuBtn.click();
  await new Promise(r => setTimeout(r, 500));

  const clicked = await page.evaluate(() => {
    const items = Array.from(document.querySelectorAll('.p-menuitem-text'));
    const item = items.find(el => el.textContent.trim() === 'Select All');
    if (!item) return 'NOT_FOUND';
    item.closest('a, li').click();
    return 'CLICKED';
  });
  console.log('menu click result:', clicked);
  await new Promise(r => setTimeout(r, 500));

  const selection = await page.evaluate(() => {
    const ta = document.querySelector('textarea.openscad-editor');
    if (!ta) return { error: 'no textarea' };
    return {
      isActive: document.activeElement === ta,
      selectionStart: ta.selectionStart,
      selectionEnd: ta.selectionEnd,
      valueLength: ta.value.length,
    };
  });
  console.log('selection state:', JSON.stringify(selection));

  const errorLogs = logs.filter(l => /\[pageerror\]|console\.error/i.test(l));
  const pass = clicked === 'CLICKED' && selection.selectionStart === 0 && selection.selectionEnd === selection.valueLength && selection.valueLength > 0;
  console.log('--- RESULT:', pass ? 'PASS' : 'FAIL', '---');

  await browser.close();
  process.exit(0);
})().catch((e) => { console.error('FATAL', e); process.exit(1); });
