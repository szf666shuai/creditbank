/**
 * 演示剧场 · 点按钮在弹层中播放 GIF/MP4
 * 纸面页保持 editorial；项目 UI 只在深色 screen 框内出现
 */
(function () {
  'use strict';

  var overlay = null;

  function isOpen() {
    return overlay && overlay.classList.contains('open');
  }

  window.__demoTheaterOpen = false;

  function buildOverlay() {
    overlay = document.createElement('div');
    overlay.className = 'demo-theater-overlay';
    overlay.innerHTML =
      '<div class="demo-theater-panel" role="dialog" aria-modal="true">' +
        '<div class="demo-theater-head">' +
          '<h3 id="demo-theater-title">操作演示</h3>' +
          '<button type="button" class="demo-theater-close" aria-label="关闭">×</button>' +
        '</div>' +
        '<div class="demo-theater-screen" id="demo-theater-screen"></div>' +
        '<div class="demo-theater-foot" id="demo-theater-foot">Esc 关闭 · 点击背景关闭</div>' +
      '</div>';
    document.body.appendChild(overlay);

    overlay.querySelector('.demo-theater-close').addEventListener('click', close);
    overlay.addEventListener('click', function (e) {
      if (e.target === overlay) close();
    });
  }

  function renderPlaceholder(title, srcHint) {
    var screen = document.getElementById('demo-theater-screen');
    screen.innerHTML =
      '<div class="demo-theater-placeholder">' +
        '<div class="ph-label">Demo · 待替换素材</div>' +
        '<div class="ph-title">' + title + '</div>' +
        '<div class="demo-mock-ui">' +
          '<div class="demo-mock-bar" style="width:90%"></div>' +
          '<div class="demo-mock-bar"></div>' +
          '<div class="demo-mock-bar"></div>' +
        '</div>' +
        (srcHint ? '<div class="ph-path">' + srcHint + '</div>' : '') +
      '</div>';
  }

  function open(opts) {
    if (!overlay) buildOverlay();
    var title = opts.title || '操作演示';
    var src = opts.src || '';
    var type = opts.type || 'auto';
    var caption = opts.caption || '';

    document.getElementById('demo-theater-title').textContent = title;
    document.getElementById('demo-theater-foot').textContent = caption || 'Esc 关闭 · 点击背景关闭';

    var screen = document.getElementById('demo-theater-screen');
    screen.innerHTML = '';

    if (!src) {
      renderPlaceholder(title, opts.placeholder || 'assets/demos/xxx.gif');
    } else if (type === 'video' || (/\.(mp4|webm)$/i.test(src))) {
      var video = document.createElement('video');
      video.src = src;
      video.controls = true;
      video.autoplay = true;
      video.loop = true;
      video.playsInline = true;
      video.onerror = function () { renderPlaceholder(title, src); };
      screen.appendChild(video);
    } else {
      var img = document.createElement('img');
      img.src = src;
      img.alt = title;
      img.onerror = function () { renderPlaceholder(title, src); };
      screen.appendChild(img);
    }

    overlay.classList.add('open');
    window.__demoTheaterOpen = true;
    window.focus();
  }

  function close() {
    if (!overlay) return;
    overlay.classList.remove('open');
    window.__demoTheaterOpen = false;
    var screen = document.getElementById('demo-theater-screen');
    if (screen) {
      var v = screen.querySelector('video');
      if (v) { v.pause(); v.removeAttribute('src'); }
      screen.innerHTML = '';
    }
  }

  document.querySelectorAll('[data-demo-open]').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      e.preventDefault();
      open({
        title: btn.getAttribute('data-demo-title') || '操作演示',
        src: btn.getAttribute('data-demo-src') || '',
        type: btn.getAttribute('data-demo-type') || 'auto',
        caption: btn.getAttribute('data-demo-caption') || '',
        placeholder: btn.getAttribute('data-demo-placeholder') || ''
      });
    });
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && isOpen()) {
      e.preventDefault();
      e.stopPropagation();
      close();
    }
  });

  window.DemoTheater = { open: open, close: close, isOpen: isOpen };
})();
