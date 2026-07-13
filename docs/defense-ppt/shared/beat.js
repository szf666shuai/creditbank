/**
 * Spotlight beat 控制器 v3
 * 支持 .beat-stack（聚光灯单卡）与 [data-bento-deck]（逐格点亮）
 */
(function () {
  'use strict';

  var stack = document.querySelector('.beat-stack');
  var bentoDeck = document.querySelector('[data-bento-deck]');
  var isBento = !!bentoDeck;
  var cards = stack ? Array.prototype.slice.call(stack.querySelectorAll('.beat-card')) : [];
  var bentoItems = bentoDeck ? Array.prototype.slice.call(bentoDeck.querySelectorAll('.bento-beat')) : [];
  var items = isBento ? bentoItems : cards;
  var hint = document.querySelector('.beat-hint');
  var progressFill = document.querySelector('.beat-progress-fill');
  var timelineFill = document.querySelector('.timeline-fill');
  var nodes = document.querySelectorAll('.t-node');
  var step = 0;
  var maxStep = items.length;

  function updateFlow() {
    var row = document.querySelector('.b-flow-row[data-flow-sync]');
    if (!row) return;
    var nodes = row.querySelectorAll('.b-flow-node');
    nodes.forEach(function (n, i) {
      n.classList.toggle('lit', i < step);
    });
  }

  function updateTimeline() {
    var pct = maxStep ? (step / maxStep) * 100 : 0;
    if (timelineFill) timelineFill.style.width = pct + '%';
    if (progressFill) progressFill.style.width = pct + '%';
    nodes.forEach(function (n, i) {
      n.classList.toggle('lit', i < step);
    });
  }

  function updateHint() {
    if (!hint || !maxStep) return;
    if (step < maxStep) {
      hint.textContent = '点击或按 →  ·  ' + step + ' / ' + maxStep;
      hint.classList.add('show');
    } else {
      hint.textContent = '→ 下一页';
      hint.classList.add('show');
    }
  }

  function showCard(index, animate) {
    if (isBento) {
      items.forEach(function (c, i) {
        c.classList.toggle('shown', i <= index);
        if (i === index && animate) {
          c.classList.add('enter');
          c.addEventListener('animationend', function fn() {
            c.classList.remove('enter');
            c.removeEventListener('animationend', fn);
          });
        }
      });
      return;
    }
    cards.forEach(function (c, i) {
      c.classList.remove('active', 'enter', 'exit');
      if (i === index) {
        c.classList.add('active');
        if (animate) {
          c.classList.add('enter');
          c.addEventListener('animationend', function fn() {
            c.classList.remove('enter');
            c.removeEventListener('animationend', fn);
          });
        }
      } else if (i === index - 1 && animate) {
        c.classList.add('exit');
      }
    });
  }

  if (maxStep) {
    step = 1;
    showCard(0, false);
    updateTimeline();
    updateFlow();
    updateHint();
  }

  function advanceBeat() {
    if (!items.length || step >= maxStep) return false;
    var prev = step - 1;
    step++;
    showCard(step - 1, true);
    if (!isBento && cards[prev]) cards[prev].classList.add('exit');
    updateTimeline();
    updateFlow();
    updateHint();
    return true;
  }

  function retreatBeat() {
    if (!items.length || step <= 1) return false;
    step--;
    showCard(step - 1, true);
    updateTimeline();
    updateFlow();
    updateHint();
    return true;
  }

  function notifyParentNext() {
    try { parent.postMessage({ type: 'deck-slide-next' }, '*'); } catch (_) {}
  }

  function notifyParentPrev() {
    try { parent.postMessage({ type: 'deck-slide-prev' }, '*'); } catch (_) {}
  }

  function onNext() {
    if (window.__demoTheaterOpen) return;
    if (advanceBeat()) return;
    notifyParentNext();
  }

  function onPrev() {
    if (window.__demoTheaterOpen) return;
    if (retreatBeat()) return;
    notifyParentPrev();
  }

  window.addEventListener('message', function (e) {
    if (window.__demoTheaterOpen) return;
    if (!e.data || e.data.type !== 'deck-key') return;
    if (e.data.key === 'ArrowRight' || e.data.key === ' ' || e.data.key === 'PageDown') onNext();
    if (e.data.key === 'ArrowLeft' || e.data.key === 'PageUp') onPrev();
  });

  document.addEventListener('keydown', function (e) {
    if (window.__demoTheaterOpen) return;
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
    if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') {
      e.preventDefault();
      onNext();
    }
    if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
      e.preventDefault();
      onPrev();
    }
  });

  var clickZone = document.createElement('div');
  clickZone.style.cssText = 'position:fixed;top:0;right:0;bottom:0;width:45%;cursor:pointer;z-index:50;';
  clickZone.addEventListener('click', function () {
    if (window.__demoTheaterOpen) return;
    onNext();
  });
  document.body.appendChild(clickZone);

  window.focus();
})();
