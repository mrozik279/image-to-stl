/**
 * Minimal host page for the official YouTube IFrame Player API. Loaded once
 * into a WebView; RN talks to it by injecting `__bridge_*` calls and reads
 * back state via `window.ReactNativeWebView.postMessage`. Kept as a single
 * inline string (no external files) so it works as a WebView `source={{html}}`
 * with zero bundler config.
 */
export const YOUTUBE_BRIDGE_HTML = `<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
  <style>
    html, body { margin: 0; padding: 0; background: #000; height: 100%; overflow: hidden; }
    #player { width: 100%; height: 100%; }
  </style>
</head>
<body>
  <div id="player"></div>
  <script>
    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    document.body.appendChild(tag);

    var player = null;
    var pendingVideoId = null;
    var tickInterval = null;

    function post(msg) {
      if (window.ReactNativeWebView) {
        window.ReactNativeWebView.postMessage(JSON.stringify(msg));
      }
    }

    function startTicking() {
      stopTicking();
      tickInterval = setInterval(function () {
        if (!player || typeof player.getCurrentTime !== 'function') return;
        try {
          post({
            type: 'tick',
            positionMillis: Math.round((player.getCurrentTime() || 0) * 1000),
            durationMillis: Math.round((player.getDuration() || 0) * 1000),
          });
        } catch (e) {}
      }, 250);
    }

    function stopTicking() {
      if (tickInterval) { clearInterval(tickInterval); tickInterval = null; }
    }

    window.onYouTubeIframeAPIReady = function () {
      player = new YT.Player('player', {
        height: '100%',
        width: '100%',
        playerVars: { playsinline: 1, controls: 1, rel: 0 },
        events: {
          onReady: function () {
            post({ type: 'ready' });
            var toLoad = pendingVideoId || window.__pendingBridgeVideoId;
            if (toLoad) {
              player.cueVideoById(toLoad);
              pendingVideoId = null;
              window.__pendingBridgeVideoId = null;
            }
          },
          onStateChange: function (e) {
            var stateNames = { '-1': 'unstarted', 0: 'ended', 1: 'playing', 2: 'paused', 3: 'buffering', 5: 'cued' };
            var name = stateNames[String(e.data)] || 'unknown';
            post({ type: 'stateChange', state: name });
            if (name === 'playing') startTicking(); else stopTicking();
            try {
              post({ type: 'rates', rates: player.getAvailablePlaybackRates() });
              post({ type: 'duration', durationMillis: Math.round((player.getDuration() || 0) * 1000) });
            } catch (e) {}
          },
          onError: function (e) {
            post({ type: 'error', data: e.data });
          },
        },
      });
    };

    // Bridge functions RN calls via injectJavaScript(...)
    window.__bridge_loadVideo = function (videoId) {
      if (player && typeof player.cueVideoById === 'function') {
        player.cueVideoById(videoId);
      } else {
        pendingVideoId = videoId;
      }
    };
    window.__bridge_play = function () { if (player) player.playVideo(); };
    window.__bridge_pause = function () { if (player) player.pauseVideo(); };
    window.__bridge_seek = function (ms) { if (player) player.seekTo(ms / 1000, true); };
    window.__bridge_setVolume = function (pct) { if (player) player.setVolume(pct); };
    window.__bridge_setRate = function (rate) { if (player) player.setPlaybackRate(rate); };
  </script>
</body>
</html>`;
