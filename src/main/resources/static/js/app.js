(function () {
  'use strict';
  const root = document.documentElement;
  const css = n => getComputedStyle(root).getPropertyValue(n).trim();
  const App = (window.App = { charts: [] });

  App.paleta = ['#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#f43f5e', '#8b5cf6', '#14b8a6', '#f97316', '#64748b', '#ec4899'];
  App.moneda = v => '$' + Number(v).toLocaleString('es-CO', { maximumFractionDigits: 0 });

  /* Lee datos incrustados en el HTML: <div id="x"><i data-n="" data-v=""></i>...</div> */
  App.leer = id => Array.from(document.querySelectorAll('#' + id + ' i')).map(e => ({
    n: e.dataset.n, v: parseFloat(e.dataset.v), a: parseFloat(e.dataset.a), b: parseFloat(e.dataset.b)
  }));

  /* Estilo global de Chart.js según el tema */
  App.preparar = function () {
    if (!window.Chart) return;
    Chart.defaults.font.family = "'Plus Jakarta Sans', system-ui, sans-serif";
    Chart.defaults.font.size = 12;
    Chart.defaults.color = css('--texto-2');
    Chart.defaults.borderColor = css('--borde');
    Chart.defaults.plugins.legend.labels.usePointStyle = true;
    Chart.defaults.plugins.legend.labels.boxWidth = 8;
    Chart.defaults.plugins.legend.labels.padding = 16;
    Chart.defaults.plugins.tooltip.backgroundColor = '#0d0f1f';
    Chart.defaults.plugins.tooltip.padding = 12;
    Chart.defaults.plugins.tooltip.cornerRadius = 10;
    Chart.defaults.plugins.tooltip.boxPadding = 6;
  };

  App.degradado = function (canvas, c1, c2) {
    const g = canvas.getContext('2d').createLinearGradient(0, 0, 0, canvas.parentElement.clientHeight || 300);
    g.addColorStop(0, c1);
    g.addColorStop(1, c2);
    return g;
  };

  /* Si no hay datos, muestra un mensaje en lugar del gráfico */
  App.vacio = function (id, datos) {
    if (datos.length) return false;
    const el = document.getElementById(id);
    if (el) el.parentElement.innerHTML = '<div class="vacio"><i class="bi bi-bar-chart"></i>Sin datos para mostrar</div>';
    return true;
  };

  App.grafico = function (id, cfg) {
    const el = document.getElementById(id);
    if (!el || !window.Chart) return null;
    const c = new Chart(el, cfg);
    App.charts.push(c);
    return c;
  };

  /* Tema claro / oscuro */
  function iconoTema() {
    const i = document.querySelector('#btnTema i');
    if (i) i.className = root.dataset.theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon-stars';
  }
  document.addEventListener('click', e => {
    if (e.target.closest('#btnTema')) {
      const nuevo = root.dataset.theme === 'dark' ? 'light' : 'dark';
      root.dataset.theme = nuevo;
      root.setAttribute('data-bs-theme', nuevo);
      try { localStorage.setItem('tema', nuevo); } catch (_) {}
      iconoTema();
      if (App.charts.length) location.reload();
    }
    const ver = e.target.closest('[data-ver-clave]');
    if (ver) {
      const inp = document.querySelector(ver.dataset.verClave);
      const mostrar = inp.type === 'password';
      inp.type = mostrar ? 'text' : 'password';
      ver.querySelector('i').className = mostrar ? 'bi bi-eye-slash' : 'bi bi-eye';
    }
    if (e.target.closest('#btnMenu')) document.body.classList.toggle('menu-abierto');
    if (e.target.classList && e.target.classList.contains('sidebar-overlay')) document.body.classList.remove('menu-abierto');
  });

  /* Buscador instantáneo en tablas */
  document.addEventListener('input', e => {
    const inp = e.target.closest('[data-filtro-tabla]');
    if (!inp) return;
    const q = inp.value.toLowerCase();
    document.querySelectorAll(inp.dataset.filtroTabla + ' tbody tr').forEach(tr => {
      if (tr.classList.contains('fila-vacia')) return;
      tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
  });

  /* Números animados */
  function contar() {
    document.querySelectorAll('[data-contar]').forEach(el => {
      const fin = parseFloat(el.dataset.contar);
      if (isNaN(fin)) return;
      const pre = el.dataset.prefijo || '';
      const dur = 900, ini = performance.now();
      const fmt = v => pre + Math.round(v).toLocaleString('es-CO');
      (function paso(t) {
        const p = Math.min((t - ini) / dur, 1);
        el.textContent = fmt(fin * (1 - Math.pow(1 - p, 3)));
        if (p < 1) requestAnimationFrame(paso);
      })(ini);
    });
  }

  /* Color por categoría (según el texto) */
  function colores() {
    document.querySelectorAll('[data-hue]').forEach(el => {
      let h = 0;
      const s = el.dataset.hue;
      for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) % 360;
      el.style.setProperty('--h', h);
    });
  }

  /* Alertas que se cierran solas */
  function alertas() {
    document.querySelectorAll('.alerta[data-auto]').forEach(a => {
      setTimeout(() => { a.style.opacity = '0'; setTimeout(() => a.remove(), 500); }, 6000);
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    iconoTema();
    App.preparar();
    contar();
    colores();
    alertas();

    const ov = document.createElement('div');
    ov.className = 'sidebar-overlay';
    document.body.appendChild(ov);

    const av = document.getElementById('avatar');
    if (av) { const n = av.textContent.trim(); av.title = n; av.textContent = (n[0] || 'U').toUpperCase(); }

    const f = document.getElementById('fechaHoy');
    if (f) f.textContent = new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });

    const arch = document.getElementById('archivo');
    if (arch) {
      const zona = arch.closest('.dropzone');
      arch.addEventListener('change', () => {
        document.getElementById('nombreArchivo').textContent = arch.files[0] ? arch.files[0].name : 'Ningún archivo seleccionado';
      });
      ['dragenter', 'dragover'].forEach(ev => arch.addEventListener(ev, () => zona.classList.add('activo')));
      ['dragleave', 'drop'].forEach(ev => arch.addEventListener(ev, () => zona.classList.remove('activo')));
    }
  });
})();