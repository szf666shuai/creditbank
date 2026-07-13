/** 封面页：接收父 deck 键盘消息 */
window.addEventListener('message', function (e) {
  if (!e.data || e.data.type !== 'deck-key') return;
  try {
    if (e.data.key === 'ArrowRight' || e.data.key === ' ' || e.data.key === 'PageDown')
      parent.postMessage({ type: 'deck-slide-next' }, '*');
    if (e.data.key === 'ArrowLeft' || e.data.key === 'PageUp')
      parent.postMessage({ type: 'deck-slide-prev' }, '*');
  } catch (_) {}
});
