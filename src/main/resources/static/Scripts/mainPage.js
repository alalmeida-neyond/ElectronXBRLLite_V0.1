let languageLabels = {};
const paginationState = {};
const dataCache = {};
const ROWS_PER_PAGE = 15;

function reinitTooltips(container = document) {
  container.querySelectorAll('[data-bs-toggle="tooltip"]').forEach((el) => {
    const instance = bootstrap.Tooltip.getInstance(el);
    if (instance) instance.dispose();
    new bootstrap.Tooltip(el);
  });
}

function hidePager() {
  const wrap = document.getElementById("paginationDiv");
  const container = document.getElementById("sharedPaginationContainer");
  if (wrap) wrap.style.display = "none";
  if (container) container.innerHTML = "";
}

function closeAllDetailRows() {
  document.querySelectorAll('.detail-row').forEach(tr => tr.style.display = 'none');
  document.getElementById('upload-area')?.classList.remove('collapsed');
  hidePager();
}

function updateLanguageLabels(language) {
  fetch(`./Languages_Files/${language}.json`, { cache: "no-store" })
    .then((r) => {
      if (!r.ok) throw new Error();
      return r.json();
    })
    .then((data) => {
      languageLabels = data;
      document
        .querySelectorAll(".resultado-filter-button")
        .forEach((button) => {
          const labelKey = button.getAttribute("data-key");
          const tooltipKey = button.getAttribute("data-tooltip-key");
          const value = button.getAttribute("data-value");
          const count = button.getAttribute("data-count");
          const label = data[labelKey] ?? value ?? "";
          const tooltip = data[tooltipKey] ?? "";
          button.textContent = count ? `${label} (${count})` : label;
          if (tooltip) {
            button.setAttribute("title", tooltip);
            button.setAttribute("data-bs-toggle", "tooltip");
            button.setAttribute("data-bs-placement", "top");
          }
        });
      document
        .querySelectorAll(".internationalization:not(.resultado-filter-button)")
        .forEach((el) => {
          const key = el.getAttribute("data-key");
          let t = data[key];
          const v = el.getAttribute("data-value");
          if (v && t) t = t.replace("{0}", v);
          if (!t) return;
          if (el.tagName === "INPUT") el.placeholder = t;
          else if (el.tagName === "OPTION") el.innerHTML = t;
          else if (el.tagName === "I") el.title = t;
          else {
            const first = el.firstChild;
            if (first && first.nodeType === Node.TEXT_NODE)
              first.nodeValue = t + " ";
            else
              el.insertBefore(document.createTextNode(t + " "), el.firstChild);
          }
        });
      reinitTooltips();
    })
    .catch(() => { });
}

function showOrUpdatePager(totalItems, stateKey, onPageChange) {
  const wrap = document.getElementById("paginationDiv");
  const container = document.getElementById("sharedPaginationContainer");
  if (!wrap || !container) return;

  const totalPages = Math.ceil(totalItems / ROWS_PER_PAGE);
  wrap.style.display = totalPages > 1 ? "block" : "none";
  container.innerHTML = "";
  if (totalPages <= 1) return;

  if (!paginationState[stateKey])
    paginationState[stateKey] = { currentPage: 1, rowsPerPage: ROWS_PER_PAGE };
  const state = paginationState[stateKey];
  if (state.currentPage > totalPages) state.currentPage = totalPages;

  const makeBtn = (label, page = null, opts = {}) => {
    const btn = document.createElement("button");
    btn.textContent = label;
    btn.className = "page-btn btn btn-sm btn-light m-1 roundButton";
    if (opts.active) btn.classList.add("active");
    btn.disabled = !!opts.disabled;
    if (!btn.disabled && page !== null) {
      btn.addEventListener("click", () => {
        paginationState[stateKey].currentPage = page;
        onPageChange(page, ROWS_PER_PAGE);
        showOrUpdatePager(totalItems, stateKey, onPageChange);
      });
    }
    return btn;
  };

  container.appendChild(
    makeBtn("«", state.currentPage > 1 ? state.currentPage - 1 : null, {
      disabled: state.currentPage === 1,
    })
  );

  const start = Math.max(1, state.currentPage - 1);
  const end = Math.min(totalPages, state.currentPage + 3);
  for (let p = start; p <= end; p++)
    container.appendChild(
      makeBtn(String(p), p, { active: p === state.currentPage })
    );

  if (end < totalPages) {
    if (end + 1 < totalPages)
      container.appendChild(makeBtn("…", null, { disabled: true }));
    container.appendChild(
      makeBtn(String(totalPages), totalPages, {
        active: state.currentPage === totalPages,
      })
    );
  }

  container.appendChild(
    makeBtn(
      "»",
      state.currentPage < totalPages ? state.currentPage + 1 : null,
      { disabled: state.currentPage === totalPages }
    )
  );
}

function renderRowsGeneric(tbody, list, stateKey, buildRow) {
  const { currentPage, rowsPerPage } = paginationState[stateKey];
  tbody.innerHTML = "";
  const start = (currentPage - 1) * rowsPerPage;
  const end = start + rowsPerPage;
  list.slice(start, end).forEach((item) => tbody.appendChild(buildRow(item)));
}

document.addEventListener("DOMContentLoaded", function () {
  const importBar = document.getElementById("importProgress");
  importBar.style.width = "0%";
  importBar.textContent = "0%";
  const validationBar = document.getElementById("validationProgress");
  validationBar.style.width = "0%";
  validationBar.textContent = "0%";
  const generationBar = document.getElementById("generationProgress");
  generationBar.style.width = "0%";
  generationBar.textContent = "0%";

  const langSelect = document.getElementById("languageSelect");
  if (langSelect) {
    langSelect.addEventListener("change", function () {
      updateLanguageLabels(langSelect.value);
    });
    updateLanguageLabels(langSelect.value || "pt");
  }

  let allIOs = [];
  const uploadArea = document.getElementById("upload-area");
  const excelFileInput = document.getElementById("file");

  const date = new Date();
  const years = {
    behindThreeYears: date.getFullYear() - 3,
    behindTwoYears: date.getFullYear() - 2,
    behindOneYear: date.getFullYear() - 1,
    currentYear: date.getFullYear(),
    forwardOneYear: date.getFullYear() + 1,
    forwardTwoYears: date.getFullYear() + 2,
    forwardThreeYears: date.getFullYear() + 3,
  };
  Object.entries(years).forEach(
    ([id, v]) => (document.getElementById(id).value = v)
  );
  const filterYearDropdown = document.getElementsByName("filterYear")[0];
  filterYearDropdown.options[1].innerHTML = years.behindThreeYears;
  filterYearDropdown.options[2].innerHTML = years.behindTwoYears;
  filterYearDropdown.options[3].innerHTML = years.behindOneYear;
  filterYearDropdown.options[4].innerHTML = years.currentYear;
  filterYearDropdown.options[5].innerHTML = years.forwardOneYear;
  filterYearDropdown.options[6].innerHTML = years.forwardTwoYears;
  filterYearDropdown.options[7].innerHTML = years.forwardThreeYears;

  ["filterYear", "filterMonth"].forEach((id) => {
    const input = document.getElementById(id);
    input.addEventListener("input", () => {
      input.value = input.value.replace(/\D/g, "");
      autoSubmit();
    });
  });

  excelFileInput.addEventListener("change", () => {
    if (excelFileInput.files.length > 0) uploadFile(excelFileInput.files[0]);
  });
  uploadArea.addEventListener("dragover", (e) => {
    e.preventDefault();
    uploadArea.classList.add("dragover");
  });
  uploadArea.addEventListener("dragleave", () =>
    uploadArea.classList.remove("dragover")
  );
  uploadArea.addEventListener("drop", (e) => {
    e.preventDefault();
    uploadArea.classList.remove("dragover");
    const f = e.dataTransfer.files[0];
    if (f) uploadFile(f);
  });
  document
    .getElementById("filterModule")
    .addEventListener("change", autoSubmit);

  let progressInterval;
  function showImportProgress(v) {
    document.getElementById("importProgressBar").style.display = "block";
    document.getElementById("validationProgressBar").style.display = "none";
    document.getElementById("generationProgressBar").style.display = "none";
    importBar.style.width = v + "%";
    importBar.textContent = v + "%";
  }
  function showValidationProgress(v) {
    document.getElementById("importProgressBar").style.display = "none";
    document.getElementById("validationProgressBar").style.display = "block";
    document.getElementById("generationProgressBar").style.display = "none";
    validationBar.style.width = v + "%";
    validationBar.textContent = v + "%";
  }
  function showGenerationProgress(v) {
    document.getElementById("importProgressBar").style.display = "none";
    document.getElementById("validationProgressBar").style.display = "none";
    document.getElementById("generationProgressBar").style.display = "block";
    generationBar.style.width = v + "%";
    generationBar.textContent = v + "%";
  }
  function startProgressPolling() {
    progressInterval = setInterval(() => {
      fetch("/importProgress")
        .then((r) => r.json())
        .then((pI) => {
          if (pI === 100) {
            fetch("/validationProgress")
              .then((r) => r.json())
              .then((pV) => {
                if (pV === 100) {
                  fetch("/generationProgress")
                    .then((r) => r.json())
                    .then((pG) => {
                      if (pG === 100) {
                        clearInterval(progressInterval);
                        document.getElementById(
                          "importProgressBar"
                        ).style.display = "none";
                        document.getElementById(
                          "validationProgressBar"
                        ).style.display = "none";
                        document.getElementById(
                          "generationProgressBar"
                        ).style.display = "none";
                      } else {
                        showGenerationProgress(pG);
                      }
                    });
                } else {
                  showValidationProgress(pV);
                }
              });
          } else if (pI >= 0 && pI < 100) {
            showImportProgress(pI);
          }
        })
        .catch(() => { });
    }, 100);
  }

  function uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);
    document.getElementById("upload-text").style.display = "none";
    document.getElementById("loading-container").style.display = "flex";
    document.getElementById("file").disabled = true;
    ["homepage", "templatesPage", "settingsPage"].forEach((id) =>
      document.getElementById(id).classList.add("isDisabled")
    );
    showImportProgress(0);
    startProgressPolling();
    fetch("/importFile/upload", { method: "POST", body: formData })
      .then((r) => {
        if (!r.ok)
          return r.text().then((t) => {
            throw new Error(t);
          });
        return r.text();
      })
      .then(() => {
        document.getElementById("logContainer").style.display = "block";
        document.getElementById("upload-text").style.display = "block";
        document.getElementById("loading-container").style.display = "none";
        document.getElementById("upload-error").style.display = "none";
        document.getElementById("file").disabled = false;
        ["homepage", "templatesPage", "settingsPage"].forEach((id) =>
          document.getElementById(id).classList.remove("isDisabled")
        );
        document.getElementById("file").value = null;
        clearInterval(progressInterval);
        [
          "importProgressBar",
          "validationProgressBar",
          "generationProgressBar",
        ].forEach((id) => (document.getElementById(id).style.display = "none"));
        fetchModulesFromBackend();
        fetchIOs();
      })
      .catch((err) => {
        document.getElementById("upload-text").style.display = "block";
        document.getElementById("loading-container").style.display = "none";
        document.getElementById("file").disabled = false;
        const box = document.getElementById("upload-error");
        if (box) {
          box.innerText = err.message;
          box.style.display = "block";
        } else {
          alert("Erro: " + err.message);
        }
      });
  }

  function fetchIOs() {
    fetch("/importFile/results")
      .then((res) => {
        if (!res.ok) throw new Error("Erro ao obter os IOs");
        return res.json();
      })
      .then((data) => {
        allIOs = data;
        populateMainTable(allIOs);
      })
      .catch(() => { });
  }

  // ✅ FIX: only one top-level definition (removed stray outer wrapper)
  function createCellCustomIOStateINOUT(ioStateId, ioStateIdVal) {
    const td = document.createElement("td");
    const icon = document.createElement("i");
    icon.classList.add("bi", "me-2");
    let bg = "lightGrey";
    if (ioStateIdVal == 1) bg = "lightgrey";
    else if (ioStateIdVal == 2) bg = "#fff3cd";
    else if (ioStateIdVal == 3) bg = "#f8d7da";
    else if (ioStateIdVal == 4) bg = "lightgrey";
    else bg = "lightgrey";

    if (ioStateId == 1)
      icon.classList.add("bi-check-circle-fill", "text-success");
    else if (ioStateId == 2)
      icon.classList.add("bi-exclamation-triangle-fill", "text-warning");
    else if (ioStateId == 3)
      icon.classList.add("bi-x-circle-fill", "text-danger");
    else if (ioStateId == 4)
      icon.classList.add("bi-clock-fill", "text-primary");
    else icon.classList.add("bi-x-circle-fill", "text-danger");
    td.appendChild(icon);
    td.style.backgroundColor = bg;
    return td;
  }

  function createCellCustomIOState(ioStateId) {
    const td = document.createElement("td");
    const icon = document.createElement("i");
    icon.classList.add("bi", "me-2");
    let bg = "lightGrey";
    if (ioStateId == 1) {
      icon.classList.add("bi-check-circle-fill", "text-success");
      bg = "lightgrey";
    } else if (ioStateId == 2) {
      icon.classList.add("bi-exclamation-triangle-fill", "text-warning");
      bg = "#fff3cd";
    } else if (ioStateId == 3) {
      icon.classList.add("bi-x-circle-fill", "text-danger");
      bg = "#f8d7da";
    } else if (ioStateId == 4) {
      icon.classList.add("bi-clock-fill", "text-primary");
      bg = "lightgrey";
    } else {
      icon.classList.add("bi-x-circle-fill", "text-danger");
    }
    td.appendChild(icon);
    td.style.backgroundColor = bg;
    return td;
  }

  function createCellCustom(value, ioStateId) {
    const td = document.createElement("td");
    let bg = "lightGrey";
    if (ioStateId == 2) bg = "#fff3cd";
    else if (ioStateId == 3) bg = "#f8d7da";
    td.style.backgroundColor = bg;
    td.textContent = value ?? "-";
    return td;
  }

  function fetchModulesFromBackend() {
    const moduleSelect = document.getElementById("filterModule");
    fetch("/importFile/modules")
      .then((r) => r.json())
      .then((modules) => {
        moduleSelect.innerHTML = `<option value="" class='internationalization' data-key='module.label'></option>`;
        modules.forEach((m) => {
          const o = document.createElement("option");
          o.value = m;
          o.textContent = m;
          moduleSelect.appendChild(o);
        });
        const selectedLang =
          document.getElementById("languageSelect")?.value || "pt";
        updateLanguageLabels(selectedLang);
      })
      .catch(() => { });
  }

  function populateMainTable(ioList) {
    const tbody = document.getElementById("validationTableBody");
    tbody.innerHTML = "";
    ioList.forEach((io) => {
      const tr = document.createElement("tr");
      const td0 = createCellCustomIOState(io[8]);
      const tdIN = createCellCustomIOStateINOUT(io[5], io[8]);
      const tdOUT = createCellCustomIOStateINOUT(io[11], io[8]);
      tdIN.classList.add("first-cell");
      tr.appendChild(tdIN);
      tr.appendChild(td0);
      tr.appendChild(tdOUT);
      tr.appendChild(createCellCustom(io[0], io[8]));
      tr.appendChild(createCellCustom(io[1], io[8]));
      tr.appendChild(createCellCustom(io[2], io[8]));
      tr.appendChild(createCellCustom(io[3], io[8]));
      const tdImp = document.createElement("td");
      const tdVal = document.createElement("td");
      const tdGen = document.createElement("td");
      tdGen.classList.add("last-cell");
      
      const hasImportLogs = io[6]===1;
      const hasValidationLogs = io[9]===1;
      const ihasGenerationLogs = io[12]===1;

      const bImp = document.createElement("span");
      bImp.innerHTML = `<span style="color:${hasImportLogs?"#0d6efd":"#676b72ff"};cursor:${hasImportLogs?"pointer":"default"};text-decoration:underline;"><i class="bi bi-box-arrow-up-right"></i></span>`;
      const bVal = document.createElement("span");
      bVal.innerHTML = `<span style="color:${hasValidationLogs?"#0d6efd":"#676b72ff"};cursor:${hasValidationLogs?"pointer":"default"};text-decoration:underline;"><i class="bi bi-box-arrow-up-right"></i></span>`;
      const bGen = document.createElement("span");
      bGen.innerHTML = `<span style="color:${ihasGenerationLogs?"#0d6efd":"#676b72ff"};cursor:${ihasGenerationLogs?"pointer":"default"};text-decoration:underline;"><i class="bi bi-box-arrow-up-right"></i></span>`;
      
      tdImp.appendChild(bImp);
      tdVal.appendChild(bVal);
      tdGen.appendChild(bGen);
      tr.appendChild(tdImp);
      tr.appendChild(tdVal);
      tr.appendChild(tdGen);

      bImp.onclick = () => hasImportLogs? toggleImportDetails(io[4], bImp): ()=>{};
      bVal.onclick = () => hasValidationLogs? toggleValidationDetails(io[7], bVal): ()=>{};
      bGen.onclick = () => ihasGenerationLogs? toggleGenerationDetails(io[10], bGen): ()=>{};

      [tdImp, tdVal, tdGen].forEach(
        (td) => (td.style.backgroundColor = "lightGrey")
      );
      if (io[1] == 2) {
        tdImp.style.backgroundColor = "#fff3cd";
        tdVal.style.backgroundColor = "#fff3cd";
        tdGen.style.backgroundColor = "#fff3cd";
      } else if (io[1] == 3) {
        tdImp.style.backgroundColor = "#f8d7da";
        tdVal.style.backgroundColor = "#f8d7da";
        tdGen.style.backgroundColor = "#f8d7da";
      }
      tbody.appendChild(tr);

      const rImp = document.createElement("tr");
      const rVal = document.createElement("tr");
      const rGen = document.createElement("tr");
      rImp.style.display = "none";
      rVal.style.display = "none";
      rGen.style.display = "none";
      rImp.className = "detail-row";
      rVal.className = "detail-row";
      rGen.className = "detail-row";
      const tdImpD = document.createElement("td");
      tdImpD.colSpan = 10;
      tdImpD.style.padding = 10;
      const tdValD = document.createElement("td");
      tdValD.colSpan = 10;
      tdValD.style.padding = 10;
      const tdGenD = document.createElement("td");
      tdGenD.colSpan = 10;
      tdGenD.style.padding = 10;
      const divImp = document.createElement("div");
      divImp.id = `detail-import-${io[4]}`;
      divImp.classList.add("internationalization");
      divImp.setAttribute("data-key", "loading.label");
      const divVal = document.createElement("div");
      divVal.id = `detail-validation-${io[7]}`;
      divVal.classList.add("internationalization");
      divVal.setAttribute("data-key", "loading.label");
      const divGen = document.createElement("div");
      divGen.id = `detail-generation-${io[10]}`;
      divGen.classList.add("internationalization");
      divGen.setAttribute("data-key", "loading.label");
      tdImpD.appendChild(divImp);
      tdValD.appendChild(divVal);
      tdGenD.appendChild(divGen);
      rImp.appendChild(tdImpD);
      rVal.appendChild(tdValD);
      rGen.appendChild(tdGenD);
      tbody.appendChild(rImp);
      tbody.appendChild(rVal);
      tbody.appendChild(rGen);
    });
  }

  function openCloseRow(container, open) {
    const row = container.closest("tr");
    row.style.display = open ? "table-row" : "none";
    document.getElementById("upload-area")?.classList[open ? "add" : "remove"]("collapsed");
    if (!open) hidePager();
  }

  function toggleValidationDetails(ioid, button) {
    const container = document.getElementById(`detail-validation-${ioid}`);
    if (!container) return;
    const row = container.closest("tr");
    const isHidden = row.style.display === "none";

    if (!isHidden) {
      row.style.display = "none";
      document.getElementById("upload-area")?.classList.remove("collapsed");
      hidePager();
      return;
    }
    closeAllDetailRows();
    openCloseRow(container, true);
    const span = button.querySelector("span");
    if (span) {
      span.innerHTML = "";
      const i = document.createElement("i");
      i.className = "bi bi-box-arrow-up-right";
      i.style.marginLeft = "6px";
      span.appendChild(i);
    }
    const lang = document.getElementById("languageSelect")?.value || "pt";
    updateLanguageLabels(lang);

    const stateKey = `validation-${ioid}`;

    const buildRow = (d) => {
      const resultado = d.resultado ?? "-";
      let rowStyle = "";
      if (resultado === "RULE OK") rowStyle = "background-color:#d4edda";
      else if (resultado === "RULE DO NOT RUN")
        rowStyle = "background-color:#fff3cd";
      else if (resultado === "RULE NOT OK")
        rowStyle = "background-color:#f8d7da";
      const tr = document.createElement("tr");
      const td1 = document.createElement("td");
      td1.style = rowStyle;
      td1.textContent = d.regraCode ?? "-";
      td1.style.fontSize = "0.7rem";
      tr.appendChild(td1);
      const td2 = document.createElement("td");
      td2.style = rowStyle;
      const icon = document.createElement("i");
      icon.classList.add("bi", "me-2");
      const sev = ((d.severity ?? "") + "").toLowerCase();
      if (sev === "ok")
        icon.classList.add("bi-check-circle-fill", "text-success");
      else if (sev === "warning")
        icon.classList.add("bi-exclamation-triangle-fill", "text-warning");
      else if (sev === "error")
        icon.classList.add("bi-x-circle-fill", "text-danger");
      td2.appendChild(icon);
      tr.appendChild(td2);
      const td3 = document.createElement("td");
      td3.style = rowStyle;
      td3.textContent = d.regraDomain ?? "-";
      td3.style.fontSize = "0.7rem";
      td3.style.overflowWrap = "break-word";
      tr.appendChild(td3);
      const td4 = document.createElement("td");
      td4.style = rowStyle;
      td4.textContent = d.regra ?? "-";
      td4.style.fontSize = "0.7rem";
      td4.classList.add("firstItemDetails");
      tr.appendChild(td4);
      const td5 = document.createElement("td");
      td5.style = rowStyle;
      td5.textContent = d.regraExecutada ?? "-";
      td5.style.fontSize = "0.7rem";
      tr.appendChild(td5);
      const td6 = document.createElement("td");
      td6.style = rowStyle;
      td6.textContent = resultado;
      td6.style.fontSize = "0.7rem";
      tr.appendChild(td6);
      const td7 = document.createElement("td");
      td7.style = rowStyle;
      td7.textContent = d.dataProcessamento ?? "-";
      td7.style.fontSize = "0.7rem";
      td7.classList.add("lastItemDetails");
      tr.appendChild(td7);
      return tr;
    };

    const ensureUI = (list) => {
      let filtered = list.slice();
      const scroll = document.createElement("div");
      scroll.className = "detailsTable";
      const table = document.createElement("table");
      table.className = "fixed-header-table table table-borderless";
      const thead = document.createElement("thead");
      thead.style.zIndex = 4;
      const hr = document.createElement("tr");
      [
        "ruleCode.label",
        "severity.label",
        "ruleDomain.label",
        "rule.label",
        "ruleValues.label",
        "result.label",
        "processDate.label",
      ].forEach((k, i) => {
        const th = document.createElement("th");
        th.classList.add("internationalization");
        th.setAttribute("data-key", k);
        th.style.fontSize = "0.9rem";
        if (i === 0) th.classList.add("firstItemDetails");
        if (i === 6) th.classList.add("lastItemDetails");
        hr.appendChild(th);
      });
      thead.appendChild(hr);
      const tbody = document.createElement("tbody");

      const counts = {};
      list.forEach((d) => {
        const k = d.resultado ?? "-";
        counts[k] = (counts[k] || 0) + 1;
      });
      const unique = [...new Set(list.map((d) => d.resultado ?? "-"))];
      const map = {
        "RULE OK": "ruleOK",
        "RULE NOT OK": "ruleNotOK",
        "RULE DO NOT RUN": "ruleSkip",
        "RULE DO NOT RUN PREREQUISITE": "ruleSkipPre",
        "RULE OK WITH NOT OK": "ruleOKNotOK",
      };
      const filters = document.createElement("div");
      filters.className = "resultado-filter-group";
      const allBtn = document.createElement("div");
      allBtn.className = "resultado-filter-button active internationalization";
      allBtn.setAttribute("data-key", "all.label");
      allBtn.setAttribute("data-value", "__all__");
      allBtn.setAttribute("data-count", list.length);
      allBtn.textContent = `All (${list.length})`;
      filters.appendChild(allBtn);
      unique.forEach((v) => {
        const keyBase = map[v] || "";
        const cnt = counts[v] || 0;
        const label = languageLabels[`${keyBase}.label`] || v;
        const b = document.createElement("div");
        b.className = "resultado-filter-button internationalization";
        b.setAttribute("data-key", `${keyBase}.label`);
        b.setAttribute("data-value", v);
        b.setAttribute("data-count", cnt);
        b.setAttribute("data-bs-toggle", "tooltip");
        b.setAttribute("data-bs-placement", "top");
        b.textContent = cnt ? `${label} (${cnt})` : label;
        filters.appendChild(b);
      });

      container.innerHTML = "";
      container.classList.remove("internationalization");
      container.removeAttribute("data-key");
      table.appendChild(thead);
      table.appendChild(tbody);
      scroll.appendChild(filters);
      scroll.appendChild(table);
      container.appendChild(scroll);

      if (!paginationState[stateKey])
        paginationState[stateKey] = {
          currentPage: 1,
          rowsPerPage: ROWS_PER_PAGE,
        };

      const doRender = () => {
        renderRowsGeneric(tbody, filtered, stateKey, buildRow);
        showOrUpdatePager(filtered.length, stateKey, (page, rpp) => {
          renderRowsGeneric(tbody, filtered, stateKey, buildRow);
        });
      };

      filters.querySelectorAll(".resultado-filter-button").forEach((btn) => {
        btn.addEventListener("click", function () {
          filters
            .querySelectorAll(".resultado-filter-button")
            .forEach((x) => x.classList.remove("active"));
          this.classList.add("active");
          const sel = (this.getAttribute("data-value") || "").toLowerCase();
          filtered = list.filter(
            (d) =>
              sel === "__all__" || (d.resultado || "").toLowerCase() === sel
          );
          paginationState[stateKey].currentPage = 1;
          doRender();
          updateLanguageLabels(
            document.getElementById("languageSelect")?.value || "pt"
          );
        });
      });

      doRender();
      updateLanguageLabels(
        document.getElementById("languageSelect")?.value || "pt"
      );
    };

    if (dataCache[stateKey]) {
      ensureUI(dataCache[stateKey]);
    } else {
      fetch(`/importFile/validation/results/${ioid}`)
        .then((r) => r.json())
        .then((details) => {
          dataCache[stateKey] = details.slice();
          ensureUI(dataCache[stateKey]);
        })
        .catch(() => {
          container.textContent =
            languageLabels["errorDetails.label"] || "Error loading details.";
          container.classList.add("internationalization");
          container.setAttribute("data-key", "errorDetails.label");
        });
    }
  }

  function toggleImportDetails(ioid, button) {
    const container = document.getElementById(`detail-import-${ioid}`);
    if (!container) return;
    const row = container.closest("tr");
    const isHidden = row.style.display === "none";

    if (!isHidden) {
      row.style.display = "none";
      document.getElementById("upload-area")?.classList.remove("collapsed");
      hidePager();                 
      return;
    }
    closeAllDetailRows();
    openCloseRow(container, true);
    const span = button.querySelector("span");
    if (span) {
      span.innerHTML = "";
      const i = document.createElement("i");
      i.className = "bi bi-box-arrow-up-right";
      i.style.marginLeft = "6px";
      span.appendChild(i);
    }
    const lang = document.getElementById("languageSelect")?.value || "pt";
    updateLanguageLabels(lang);

    const stateKey = `import-${ioid}`;

    const buildRow = (d) => {
      const tr = document.createElement("tr");
      const td1 = document.createElement("td");
      td1.textContent = d.code ?? "-";
      td1.style.fontSize = "0.7rem";
      tr.appendChild(td1);
      const td2 = document.createElement("td");
      td2.textContent = d.entity ?? "-";
      td2.style.fontSize = "0.7rem";
      tr.appendChild(td2);
      const td3 = document.createElement("td");
      td3.textContent = d.domain ?? "-";
      td3.style.fontSize = "0.7rem";
      tr.appendChild(td3);
      const td4 = document.createElement("td");
      td4.textContent = d.referenceDate ?? "-";
      td4.style.fontSize = "0.7rem";
      tr.appendChild(td4);
      const td5 = document.createElement("td");
      td5.textContent = d.description ?? "-";
      td5.style.fontSize = "0.7rem";
      tr.appendChild(td5);
      const td6 = document.createElement("td");
      td6.textContent = d.timestamp ?? "-";
      td6.style.fontSize = "0.7rem";
      tr.appendChild(td6);
      return tr;
    };

    const ensureUI = (list) => {
      const scroll = document.createElement("div");
      scroll.className = "detailsTable";
      const table = document.createElement("table");
      table.className = "fixed-header-table table table-borderless";
      const thead = document.createElement("thead");
      thead.style.zIndex = 4;
      const hr = document.createElement("tr");
      [
        "module.label",
        "entity.label",
        "domain.label",
        "referenceDate.label",
        "description.label",
        "date.label",
      ].forEach((k, i) => {
        const th = document.createElement("th");
        th.classList.add("internationalization");
        th.setAttribute("data-key", k);
        th.style.fontSize = "0.9rem";
        if (i === 0) th.classList.add("firstItemDetails");
        if (i === 5) th.classList.add("lastItemDetails");
        hr.appendChild(th);
      });
      thead.appendChild(hr);
      const tbody = document.createElement("tbody");
      container.innerHTML = "";
      container.classList.remove("internationalization");
      container.removeAttribute("data-key");

      if (!paginationState[stateKey])
        paginationState[stateKey] = {
          currentPage: 1,
          rowsPerPage: ROWS_PER_PAGE,
        };
      const doRender = () => {
        renderRowsGeneric(tbody, list, stateKey, buildRow);
        showOrUpdatePager(list.length, stateKey, (page, rpp) => {
          renderRowsGeneric(tbody, list, stateKey, buildRow);
        });
      };

      table.appendChild(thead);
      table.appendChild(tbody);
      scroll.appendChild(table);
      container.appendChild(scroll);
      
      doRender();
      updateLanguageLabels(
        document.getElementById("languageSelect")?.value || "pt"
      );
    };

    if (dataCache[stateKey]) {
      ensureUI(dataCache[stateKey]);
    } else {
      fetch(`/importFile/import/results/${ioid}`)
        .then((r) => r.json())
        .then((details) => {
          dataCache[stateKey] = details.slice();
          ensureUI(dataCache[stateKey]);
        })
        .catch(() => {
          container.textContent =
            languageLabels["errorDetails.label"] || "Error loading details.";
          container.classList.add("internationalization");
          container.setAttribute("data-key", "errorDetails.label");
        });
    }
  }

  function toggleGenerationDetails(ioid, button) {
    const container = document.getElementById(`detail-generation-${ioid}`);
    if (!container) return;
    const row = container.closest("tr");
    const isHidden = row.style.display === "none";

    if (!isHidden) {
      row.style.display = "none";
      document.getElementById("upload-area")?.classList.remove("collapsed");
      hidePager();                 
      return;
    }
    closeAllDetailRows();
    openCloseRow(container, true);
    const span = button.querySelector("span");
    if (span) {
      span.innerHTML = "";
      const i = document.createElement("i");
      i.className = "bi bi-box-arrow-up-right";
      i.style.marginLeft = "6px";
      span.appendChild(i);
    }
    const lang = document.getElementById("languageSelect")?.value || "pt";
    updateLanguageLabels(lang);

    const stateKey = `generation-${ioid}`;

    const buildRow = (d) => {
      const tr = document.createElement("tr");
      const td1 = document.createElement("td");
      td1.textContent = d.code ?? "-";
      td1.style.fontSize = "0.7rem";
      tr.appendChild(td1);
      const td2 = document.createElement("td");
      td2.textContent = d.entity ?? "-";
      td2.style.fontSize = "0.7rem";
      tr.appendChild(td2);
      const td3 = document.createElement("td");
      td3.textContent = d.domain ?? "-";
      td3.style.fontSize = "0.7rem";
      tr.appendChild(td3);
      const td4 = document.createElement("td");
      td4.textContent = d.referenceDate ?? "-";
      td4.style.fontSize = "0.7rem";
      tr.appendChild(td4);
      const td5 = document.createElement("td");
      td5.textContent = d.description ?? "-";
      td5.style.fontSize = "0.7rem";
      tr.appendChild(td5);
      const td6 = document.createElement("td");
      td6.textContent = d.timestamp ?? "-";
      td6.style.fontSize = "0.7rem";
      tr.appendChild(td6);
      return tr;
    };

    const ensureUI = (list) => {
      const scroll = document.createElement("div");
      scroll.className = "detailsTable";
      const table = document.createElement("table");
      table.className = "fixed-header-table table table-borderless";
      const thead = document.createElement("thead");
      thead.style.zIndex = 4;
      const hr = document.createElement("tr");
      [
        "module.label",
        "entity.label",
        "domain.label",
        "referenceDate.label",
        "description.label",
        "date.label",
      ].forEach((k, i) => {
        const th = document.createElement("th");
        th.classList.add("internationalization");
        th.setAttribute("data-key", k);
        th.style.fontSize = "0.9rem";
        if (i === 0) th.classList.add("firstItemDetails");
        if (i === 5) th.classList.add("lastItemDetails");
        hr.appendChild(th);
      });
      thead.appendChild(hr);
      const tbody = document.createElement("tbody");
      container.innerHTML = "";
      container.classList.remove("internationalization");
      container.removeAttribute("data-key");
      
      if (!paginationState[stateKey])
        paginationState[stateKey] = {
          currentPage: 1,
          rowsPerPage: ROWS_PER_PAGE,
        };
      const doRender = () => {
        renderRowsGeneric(tbody, list, stateKey, buildRow);
        showOrUpdatePager(list.length, stateKey, (page, rpp) => {
          renderRowsGeneric(tbody, list, stateKey, buildRow);
        });
      };

      table.appendChild(thead);
      table.appendChild(tbody);
      scroll.appendChild(table);
      container.appendChild(scroll);
      doRender();
      updateLanguageLabels(
        document.getElementById("languageSelect")?.value || "pt"
      );
    };

    if (dataCache[stateKey]) {
      ensureUI(dataCache[stateKey]);
    } else {
      fetch(`/importFile/generation/results/${ioid}`)
        .then((r) => r.json())
        .then((details) => {
          dataCache[stateKey] = details.slice();
          ensureUI(dataCache[stateKey]);
        })
        .catch(() => {
          container.textContent =
            languageLabels["errorDetails.label"] || "Error loading details.";
          container.classList.add("internationalization");
          container.setAttribute("data-key", "errorDetails.label");
        });
    }
  }

  fetchModulesFromBackend();
  fetchIOs();

  function extractYearMonth(dateStr) {
    if (!dateStr) return { y: "", m: "" };
    const m = String(dateStr).match(/^(\d{4})-(\d{1,2})(?:-(\d{1,2}))?/);
    if (!m) return { y: "", m: "" };
    const y = m[1];
    const month = m[2].padStart(2, "0");
    return { y, m: month };
  }
  // Small alias to match existing calls
  function referenceYearMonth(dateStr){ return extractYearMonth(dateStr); }

  function autoSubmit() {
    const moduleFilter = document
      .getElementById("filterModule")
      .value.trim()
      .toLowerCase();
    const selectedYear = (
      document.getElementsByName("filterYear")[0].value || ""
    ).trim();
    const selectedMonthRaw = document.getElementById("filterMonth").value || "";
    const selectedMonth = selectedMonthRaw
      ? String(selectedMonthRaw).padStart(2, "0")
      : "";
    const filtered = allIOs.filter((io) => {
      const moduleValue = (io[2] ?? "").toLowerCase().trim();
      const dateValue = (io[5] ?? "").trim();
      const moduleMatch = !moduleFilter || moduleValue.includes(moduleFilter);
      const { y, m } = extractYearMonth(dateValue);
      const yearMatch = !selectedYear || y === selectedYear;
      const monthMatch = !selectedMonth || m === selectedMonth;
      return moduleMatch && yearMatch && monthMatch;
    });
    populateMainTable(filtered);
  }
});
