window.I18N = window.I18N || (() => {
  const KEY = 'lang';
  const SUPPORTED = ['pt','en','es','fr','it','de'];

  function get() {
    const saved = localStorage.getItem(KEY);
    if (saved && SUPPORTED.includes(saved)) return saved;
    const br = (navigator.language || 'pt').slice(0,2).toLowerCase();
    return SUPPORTED.includes(br) ? br : 'pt';
  }
  function set(l) {
    if (!SUPPORTED.includes(l)) return;
    localStorage.setItem(KEY, l);
    try { document.documentElement.setAttribute('lang', l); } catch {}
  }
  const api = { get, set, SUPPORTED };
  try { document.documentElement.setAttribute('lang', get()); } catch {}
  return api;
})();

let languageLabels = {};

function updateLanguageLabels(language) {
  fetch(`./Languages_Files/${language}.json`)
    .then(r => { if (!r.ok) throw new Error("Ficheiro de idioma não encontrado"); return r.json(); })
    .then(data => {
      document.querySelectorAll(".internationalization").forEach(element => {
        const key = element.getAttribute("data-key");
        if (!key) return;
        let translation = data[key];
        if (translation == null) return;

        const value = element.getAttribute("data-value");
        if (value) translation = translation.replace("{0}", value);

        if (element.tagName === "INPUT") {
          const type = element.getAttribute("type")?.toLowerCase();
          if (type === "submit" || type === "button") element.value = translation;
          else element.placeholder = translation;
        } else if (element.tagName === "I") {
          element.title = translation;
        } else {
          element.textContent = translation;
        }
      });
    })
    .catch(err => console.error("Erro ao carregar idioma:", err));
}

function lastDayOfMonth(y, m) { return new Date(y, m, 0).getDate(); }

function ymdToNumber(ymd) {
  const parts = (ymd || "").split("-");
  if (parts.length !== 3) return null;
  const [Y, M, D] = parts;
  if (!/^\d{4}$/.test(Y) || !/^\d{1,2}$/.test(M) || !/^\d{1,2}$/.test(D)) return null;
  return Number(`${Y.padStart(4,"0")}${M.padStart(2,"0")}${D.padStart(2,"0")}`);
}

function computeReferenceDateNumber(yearStr, monthStr) {
  if (!yearStr || !monthStr) return null;
  const y = parseInt(yearStr, 10), m = parseInt(monthStr, 10);
  if (isNaN(y) || isNaN(m) || m < 1 || m > 12) return null;
  const day = lastDayOfMonth(y, m);
  return ymdToNumber(`${y}-${String(m).padStart(2,"0")}-${String(day).padStart(2,"0")}`);
}

function populateYears() {
  const yNow = new Date().getFullYear();
  [
    ["behindThreeYears", yNow - 3],
    ["behindTwoYears",   yNow - 2],
    ["behindOneYear",    yNow - 1],
    ["currentYear",      yNow],
    ["forwardOneYear",   yNow + 1],
    ["forwardTwoYears",  yNow + 2],
    ["forwardThreeYears",yNow + 3],
  ].forEach(([id, y]) => {
    const opt = document.getElementById(id);
    if (opt) { opt.value = String(y); opt.textContent = String(y); }
  });
}

function fetchModulesFromBackend() {
  const moduleSelect = document.getElementById("filterModule");
  if (!moduleSelect) return;

  fetch("/importFile/modules")
    .then(async (r) => {
      try { return await r.json(); }
      catch (e) {
        const txt = await r.text();
        console.error("Modules endpoint did not return JSON. Raw response:", txt);
        throw e;
      }
    })
    .then((raw) => {
      const mods = Array.isArray(raw) ? raw
                  : (raw && typeof raw === "object") ? Object.values(raw)
                  : [];

      moduleSelect.innerHTML =
        `<option value="" class="internationalization" data-key="allModules.label"></option>`;

      mods.forEach((m) => {
        let label, code;
        if (m && typeof m === "object") {
          code  = String(m.code ?? m.name ?? m.moduleCode ?? m.moduleName ?? "");
          label = String(m.name ?? m.moduleName ?? m.code ?? code);
        } else {
          code = String(m ?? "");
          label = code;
        }
        if (!code.trim()) return;

        const opt = document.createElement("option");
        opt.value = code;
        opt.textContent = label;
        opt.setAttribute("data-code", code);
        moduleSelect.appendChild(opt);
      });

      updateLanguageLabels(window.I18N.get());

      const pre = moduleSelect.getAttribute("data-selected");
      if (pre != null && pre !== "") moduleSelect.value = String(pre);

      filterTemplates();
    })
    .catch(err => {
      console.error("Erro ao buscar módulos:", err);
    });
}

function fetchVersionsFromBackend() {
  const versionSelect = document.getElementById("filterVersion");
  if (!versionSelect) return;

  fetch("/importFile/versions")
    .then(res => res.json())
    .then(versions => {
      let defaultOpt = versionSelect.querySelector('option[value=""]');
      if (!defaultOpt) {
        defaultOpt = document.createElement("option");
        defaultOpt.value = "";
        defaultOpt.classList.add("internationalization");
        defaultOpt.setAttribute("data-key", "version.label");
        versionSelect.prepend(defaultOpt);
      } else {
        defaultOpt.classList.add("internationalization");
        defaultOpt.setAttribute("data-key", "version.label");
      }

      [...versionSelect.querySelectorAll('option:not([value=""])')].forEach(o => o.remove());
      versions.forEach(v => {
        const opt = document.createElement("option");
        opt.value = String(v);
        opt.textContent = String(v);
        versionSelect.appendChild(opt);
      });

      updateLanguageLabels(window.I18N.get());

      const preselected = versionSelect.getAttribute("data-selected");
      if (preselected) versionSelect.value = String(preselected);

      filterTemplates();
    })
    .catch(err => console.error("Erro ao buscar versões:", err));
}

function filterTemplates() {
  const moduleSel = document.getElementById("filterModule");
  const yearSel   = document.getElementById("filterYear");
  const monthSel  = document.getElementById("filterMonth");
  const versionInput  = document.getElementById("versionInput") || document.querySelector('input[name="version"]');
  const versionSelect = document.getElementById("filterVersion");

  const selOpt = moduleSel?.options[moduleSel.selectedIndex];
  const selectedCode = (selOpt?.dataset?.code ?? "").trim();

  const norm = s => (s || "").replace(/_/g, "").toLowerCase();
  const selectedCodeNorm = norm(selectedCode);

  const yearVal  = (yearSel?.value ?? "").trim();
  const monthVal  = (monthSel?.value ?? "").trim();
  const y = yearVal ? parseInt(yearVal, 10) : null;
  const m = monthVal ? parseInt(monthVal, 10) : null;

  let versionVal = "";
  if (versionInput && versionInput.value) {
    versionVal = versionInput.value.trim().toLowerCase();
  } else if (versionSelect && versionSelect.value) {
    versionVal = versionSelect.value.trim().toLowerCase();
  }

  const tbody = document.getElementById("templatesTbody") || document.querySelector("#templatesTable tbody");
  if (!tbody) return;

  const rows = Array.from(tbody.querySelectorAll("tr")).filter(tr => tr.id !== "noFilteredRows");
  let visibleCount = 0;

  rows.forEach(tr => {
    let show = true;

    if (show && selectedCodeNorm) {
      const rowFilename =
        (tr.getAttribute("data-filename") || "").trim()
        || tr.querySelector("td")?.textContent?.trim()
        || "";

      const mMatch = rowFilename.match(/LEI_([A-Za-z0-9_]+)_Domain/i);
      const word = mMatch ? mMatch[1] : "";
      const wordNorm = norm(word);

      show = (wordNorm && wordNorm === selectedCodeNorm);
    }

    if (show && versionVal) {
      const rowVersion = (tr.getAttribute("data-version") || "").trim().toLowerCase();
      show = rowVersion.includes(versionVal);
    }

    if (show && (y !== null || m !== null)) {
      const rowFrom = (tr.getAttribute("data-from") || "").trim().replace(/^\+/, "");
      const [ryStr, rmStr] = rowFrom.split("-");
      const ry = ryStr ? parseInt(ryStr, 10) : null;
      const rm = rmStr ? parseInt(rmStr, 10) : null;

      if (!ry || !rm) {
        show = false;
      } else if (y !== null && m !== null) {
        show = (ry === y && rm === m);
      } else if (y !== null) {
        show = (ry === y);
      } else if (m !== null) {
        show = (rm === m);
      }
    }

    tr.style.display = show ? "" : "none";
    if (show) visibleCount++;
  });

  const emptyRow = document.getElementById("noFilteredRows");
  if (emptyRow) emptyRow.style.display = (visibleCount === 0 ? "" : "none");
}

function getDownloadColumnIndex(table) {
  const headRow = table.tHead?.rows?.[0];
  if (!headRow) return -1;

  const ths = Array.from(headRow.cells || []);

  let idx = ths.findIndex(th => th.id === "downloadColumn");
  if (idx >= 0) return idx;

  idx = ths.findIndex(th =>
    th.classList.contains("download-col") ||
    th.getAttribute("data-key") === "download.label"
  );
  if (idx >= 0) return idx;

  idx = ths.findIndex(th => (th.textContent || "").trim().toLowerCase() === "download");
  if (idx >= 0) return idx;

  if (ths.length > 0) return ths.length - 1;

  return -1;
}

function populateDownloadButtons() {
  const table = document.getElementById("templatesTable");
  const tbody = document.getElementById("templatesTbody");
  if (!table || !tbody || !table.tHead) return;

  const colIndex = getDownloadColumnIndex(table);
  if (colIndex < 0) return;

  const rows = Array.from(tbody.querySelectorAll("tr")).filter(r => r.id !== "noFilteredRows");
  rows.forEach(tr => {
    const cells = tr.cells;
    let cell;
    if (cells.length > colIndex) {
      cell = cells[colIndex];
    } else {
      cell = tr.insertCell(colIndex);
    }

    if (cell.querySelector(".dl-btn")) return;

    cell.classList.add("text-center");

    const filename =
      (tr.getAttribute("data-filename") || "").trim()
      || tr.querySelector("td")?.textContent?.trim()
      || "";

    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "btn btn-sm btn-outline-primary dl-btn";
    btn.setAttribute("data-filename", filename);
    btn.innerHTML = `<i class="bi bi-download"></i>`;
    if (!filename) btn.disabled = true;

    cell.innerHTML = "";
    cell.appendChild(btn);
  });
}

function ensureMessageHost() {
  let host = document.getElementById("messageHost");
  if (!host) {
    host = document.createElement("div");
    host.id = "messageHost";
    const container = document.querySelector(".container") || document.body;
    container.prepend(host);
  }
  return host;
}

function showI18nMessage(kind, key, value) {
  const host = ensureMessageHost();

  const wrapper = document.createElement("div");
  wrapper.className = `alert alert-${kind} alert-dismissible fade show`;
  wrapper.setAttribute("role", "alert");
  wrapper.style.marginTop = ".75rem";

  const span = document.createElement("span");
  span.className = "internationalization";
  span.setAttribute("data-key", key);
  if (value != null && value !== "") span.setAttribute("data-value", String(value));

  const closeBtn = document.createElement("button");
  closeBtn.type = "button";
  closeBtn.className = "btn-close";
  closeBtn.setAttribute("data-bs-dismiss", "alert");
  closeBtn.setAttribute("aria-label", "Close");

  closeBtn.addEventListener('click',function(){
      document.getElementById("table-wrapper-templates")?.classList.remove("message");
  });

  wrapper.appendChild(span);
  wrapper.appendChild(closeBtn);

  host.innerHTML = "";
  host.appendChild(wrapper);

  updateLanguageLabels(window.I18N.get());
}

function bindFilterEventsOnce() {
  const moduleSel    = document.getElementById("filterModule");
  const yearSel      = document.getElementById("filterYear");
  const monthSel     = document.getElementById("filterMonth");
  const versionInput = document.getElementById("versionInput") || document.querySelector('input[name="version"]');
  const versionSel   = document.getElementById("filterVersion");

  const safeBind = (el, evt, handler, flagName) => {
    if (!el || el[flagName]) return;
    el.addEventListener(evt, handler);
    el[flagName] = true;
  };

  [moduleSel, yearSel, monthSel, versionSel].forEach(el =>
    safeBind(el, "change", () => { filterTemplates(); }, "_boundChange")
  );

  safeBind(versionInput, "input", filterTemplates, "_boundInput");

  [yearSel, monthSel].forEach(el =>
    safeBind(el, "input", () => {
      el.value = el.value.replace(/\D/g, "");
      filterTemplates();
    }, "_boundNumeric")
  );
}

document.addEventListener("DOMContentLoaded", () => {
  const langSelect = document.getElementById("languageSelect");
  if (langSelect) {
    langSelect.value = window.I18N.get();
    langSelect.addEventListener("change", () => {
      window.I18N.set(langSelect.value);
      updateLanguageLabels(langSelect.value);
    });
    updateLanguageLabels(window.I18N.get());
  }

  populateYears();
  bindFilterEventsOnce();

  fetchModulesFromBackend();
  fetchVersionsFromBackend();

  populateDownloadButtons();

  filterTemplates();

  const downloadSuccessMessageButton= document.getElementsByClassName("btn-close")[0];

  const table = document.getElementById("templatesTable");
  if (table && !table._dlBound) {
    table._dlBound = true;
    table.addEventListener("click", async (e) => {
      const btn = e.target.closest(".dl-btn");
      if (!btn) return;

      const filename = btn.getAttribute("data-filename") || "";
      if (!filename) {
        showI18nMessage("danger", "downloadMissingFile.label");
        return;
      }

      btn.disabled = true;
      const oldHTML = btn.innerHTML;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>`;

      document.getElementById("table-wrapper-templates")?.classList.add("message");

      try {
        const url = `/templates/download?filename=${encodeURIComponent(filename)}`;
        const res = await fetch(url, { method: "POST" });
        const data = await res.json().catch(() => ({}));

        if (!res.ok || data.ok === false) {
          const reason = data.message || `HTTP ${res.status}`;
          showI18nMessage("danger", "downloadFailedWithReason.label", reason);
        } else {
          showI18nMessage("success", "downloadSuccess.label", filename);
        }
      } catch (err) {
        console.error(err);
        showI18nMessage("danger", "networkError.label");
      } finally {
        btn.innerHTML = oldHTML;
        btn.disabled = false;
      }
    });
  }
});
