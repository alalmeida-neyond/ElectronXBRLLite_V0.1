let languageLabels = {};
let currentOpenDetailIOId = null;
let currentDetailFilteredData = [];
let currentDetailCurrentPage = 1;
let currentDetailRowsPerPage = 15;

function reinitTooltips(container = document) {
  container.querySelectorAll('[data-bs-toggle="tooltip"]').forEach((el) => {
    const instance = bootstrap.Tooltip.getInstance(el);
    if (instance) instance.dispose();
    new bootstrap.Tooltip(el);
  });
}
function updateLanguageLabels(language) {
  fetch(`./Languages_Files/${language}.json`, { cache: "no-store" })
    .then((response) => {
      if (!response.ok) throw new Error("Ficheiro de idioma não encontrado");
      return response.json();
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
        .forEach((element) => {
          const key = element.getAttribute("data-key");
          let translation = data[key];

          const value = element.getAttribute("data-value");

          if (value) {
            translation = translation.replace("{0}", value);
          }

          if (translation) {
            if (element.tagName === "INPUT") {
              element.placeholder = translation;
            } else if (element.tagName === "OPTION") {
              element.innerHTML = translation;
            } else if (element.tagName === "I") {
              element.title = translation;
            } else {
              const firstChild = element.firstChild;
              if (firstChild && firstChild.nodeType === Node.TEXT_NODE) {
                firstChild.nodeValue = translation + " ";
              } else {
                const textNode = document.createTextNode(translation + " ");
                element.insertBefore(textNode, element.firstChild);
              }
            }
          }
        });

      reinitTooltips();
    })
    .catch((error) => {
      console.error("Erro ao carregar idioma:", error);
    });
}
document.addEventListener("DOMContentLoaded", function () {
  const importBar = document.getElementById("importProgress");
  importBar.style.width = 0 + "%";
  importBar.textContent = 0 + "%";

  const validationBar = document.getElementById("validationProgress");
  validationBar.style.width = 0 + "%";
  validationBar.textContent = 0 + "%";

  const generationBar = document.getElementById("generationProgress");
  generationBar.style.width = 0 + "%";
  generationBar.textContent = 0 + "%";

  const langSelect = document.getElementById("languageSelect");

  if (langSelect) {
    langSelect.addEventListener("change", function () {
      const selectedLang = langSelect.value;
      updateLanguageLabels(selectedLang);
    });

    const initialLang = langSelect.value || "pt";
    updateLanguageLabels(initialLang);
  } else {
    console.warn("Elemento #languageSelect não encontrado");
  }

  let allIOs = [];
  const uploadArea = document.getElementById("upload-area");
  const excelFileInput = document.getElementById("file");

  var date = new Date();

  const behindThreeYears = document.getElementById("behindThreeYears");
  const behindTwoYears = document.getElementById("behindTwoYears");
  const behindOneYear = document.getElementById("behindOneYear");
  const currentYear = document.getElementById("currentYear");
  const forwardOneYear = document.getElementById("forwardOneYear");
  const forwardTwoYears = document.getElementById("forwardTwoYears");
  const forwardThreeYears = document.getElementById("forwardThreeYears");

  behindThreeYears.value = date.getFullYear() - 3;
  behindTwoYears.value = date.getFullYear() - 2;
  behindOneYear.value = date.getFullYear() - 1;
  currentYear.value = date.getFullYear();
  forwardOneYear.value = date.getFullYear() + 1;
  forwardTwoYears.value = date.getFullYear() + 2;
  forwardThreeYears.value = date.getFullYear() + 3;

  var filterYearDropdown = document.getElementsByName("filterYear")[0];

  filterYearDropdown.options[1].innerHTML = behindThreeYears.value;
  filterYearDropdown.options[2].innerHTML = behindTwoYears.value;
  filterYearDropdown.options[3].innerHTML = behindOneYear.value;
  filterYearDropdown.options[4].innerHTML = currentYear.value;
  filterYearDropdown.options[5].innerHTML = forwardOneYear.value;
  filterYearDropdown.options[6].innerHTML = forwardTwoYears.value;
  filterYearDropdown.options[7].innerHTML = forwardThreeYears.value;

  ["filterYear", "filterMonth"].forEach((id) => {
    const input = document.getElementById(id);
    input.addEventListener("input", () => {
      input.value = input.value.replace(/\D/g, "");
      autoSubmit();
    });
  });

  excelFileInput.addEventListener("change", () => {
    if (excelFileInput.files.length > 0) {
      uploadFile(excelFileInput.files[0]);
    }
  });

  uploadArea.addEventListener("dragover", (e) => {
    e.preventDefault();
    uploadArea.classList.add("dragover");
  });

  uploadArea.addEventListener("dragleave", () => {
    uploadArea.classList.remove("dragover");
  });

  uploadArea.addEventListener("drop", (e) => {
    e.preventDefault();
    uploadArea.classList.remove("dragover");
    const file = e.dataTransfer.files[0];
    if (file) {
      uploadFile(file);
    }
  });

  document
    .getElementById("filterModule")
    .addEventListener("change", autoSubmit);

  function showImportProgress(value) {
    document.getElementById("importProgressBar").style.display = "block";
    document.getElementById("validationProgressBar").style.display = "none";
    document.getElementById("generationProgressBar").style.display = "none";
    const bar = document.getElementById("importProgress");
    bar.style.width = value + "%";
    bar.textContent = value + "%";
  }

  function showValidationProgress(value) {
    document.getElementById("importProgressBar").style.display = "none";
    document.getElementById("validationProgressBar").style.display = "block";
    document.getElementById("generationProgressBar").style.display = "none";
    const bar = document.getElementById("validationProgress");
    bar.style.width = value + "%";
    bar.textContent = value + "%";
  }

  function showGenerationProgress(value) {
    document.getElementById("importProgressBar").style.display = "none";
    document.getElementById("validationProgressBar").style.display = "none";
    document.getElementById("generationProgressBar").style.display = "block";
    const bar = document.getElementById("generationProgress");
    bar.style.width = value + "%";
    bar.textContent = value + "%";
  }

  let progressInterval;

  function startProgressPolling() {
    progressInterval = setInterval(() => {
      fetch("/importProgress")
        .then((response) => response.json())
        .then((progressImport) => {
          if (progressImport === 100) {
            fetch("/validationProgress")
              .then((response) => response.json())
              .then((progressValidation) => {
                if (progressValidation === 100) {
                  fetch("/generationProgress")
                    .then((response) => response.json())
                    .then((progressGeneration) => {
                      if (progressGeneration === 100) {
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
                        showGenerationProgress(progressGeneration);
                      }
                    });
                } else {
                  showValidationProgress(progressValidation);
                }
              });
          } else if (progressImport >= 0 && progressImport < 100) {
            showImportProgress(progressImport);
          }
        })
        .catch((err) => console.error("Progress fetch error:", err));
    }, 100);
  }

  function uploadFile(file) {
    const formData = new FormData();
    formData.append("file", file);

    document.getElementById("upload-text").style.display = "none";
    document.getElementById("loading-container").style.display = "flex";
    document.getElementById("file").disabled = true;
    const homepageButton = document.getElementById("homepage");
    const templatesPageButton = document.getElementById("templatesPage");
    const settingsPageButton = document.getElementById("settingsPage");

    homepageButton.classList.add("isDisabled");
    templatesPageButton.classList.add("isDisabled");
    settingsPageButton.classList.add("isDisabled");

    showImportProgress(0);

    startProgressPolling();

    fetch("/importFile/upload", {
      method: "POST",
      body: formData,
    })
      .then((response) => {
        if (!response.ok) {
          return response.text().then((text) => {
            throw new Error(text);
          });
        }
        return response.text();
      })
      .then((data) => {
        document.getElementById("logContainer").style.display = "block";
        document.getElementById("upload-text").style.display = "block";
        document.getElementById("loading-container").style.display = "none";
        document.getElementById("upload-error").style.display = "none";
        document.getElementById("file").disabled = false;

        homepageButton.classList.add("isDisabled")?.classList.remove("isDisabled");
        templatesPageButton.classList.add("isDisabled")?.classList.remove("isDisabled");
        settingsPageButton.classList.add("isDisabled")?.classList.remove("isDisabled");

        document.getElementById("file").value = null;

        clearInterval(progressInterval);

        const importBar = document.getElementById("importProgressBar");
        const validationBar = document.getElementById("validationProgressBar");
        const generationBar = document.getElementById("generationProgressBar");

        importBar.style.display = "none";
        validationBar.style.display = "none";
        generationBar.style.display = "none";

        fetchModulesFromBackend();
        fetchIOs();
      })
      .catch((error) => {
        console.error("Erro ao fazer upload:", error.message);
        document.getElementById("upload-text").style.display = "block";
        document.getElementById("loading-container").style.display = "none";
        document.getElementById("file").disabled = false;

        const errorBox = document.getElementById("upload-error");
        if (errorBox) {
          errorBox.innerText = error.message;
          errorBox.style.display = "block";
        } else {
          alert("Erro: " + error.message);
        }
      });
  }

  function fetchIOs() {
    fetch("/importFile/results")
      .then((res) => {
        if (res.ok != true) {
          throw new Error("Erro ao obter os IOs");
        }
        return res.json();
      })
      .then((data) => {
        allIOs = data;
        populateMainTable(allIOs);
      })
      .catch((err) => {
        console.error("Erro ao buscar os IOs:", err);
      });
  }

  function createCellCustomIOState(value, ioStateId) {
    const td = document.createElement("td");
    td.classList.add("first-cell");

    const icon = document.createElement("i");
    icon.classList.add("bi", "me-2");

    let label = "";
    let backgroundColor = "";

    if (ioStateId == 1) {
      icon.classList.add("bi-check-circle-fill", "text-success");
      backgroundColor = "lightgrey";
    } else if (ioStateId == 2) {
      icon.classList.add("bi-exclamation-triangle-fill", "text-warning");
      backgroundColor = "#fff3cd";
    } else if (ioStateId == 3) {
      icon.classList.add("bi-x-circle-fill", "text-danger");
      backgroundColor = "#f8d7da";
    } else if (ioStateId == 4) {
      icon.classList.add("bi-clock-fill", "text-primary");
      backgroundColor = "#lightgrey";
    }
    else {
      icon.classList.add("bi-x-circle-fill", "text-danger");
      backgroundColor = "lightGrey";
    }

    td.appendChild(icon);
    td.style.backgroundColor = backgroundColor;

    return td;
  }

  function createCellCustom(value, ioStateId) {
    const td = document.createElement("td");

    let iconClass = "";
    let text = value ?? "-";

    if (ioStateId == 2) {
      td.style.backgroundColor = "#fff3cd";
    } else if (ioStateId == 3) {
      td.style.backgroundColor = "#f8d7da";
    } else {
      td.style.backgroundColor = "lightGrey";
    }

    td.textContent = text;
    return td;
  }

  function fetchModulesFromBackend() {
    const moduleSelect = document.getElementById("filterModule");
    fetch("/importFile/modules")
      .then((res) => res.json())
      .then((modules) => {
        moduleSelect.innerHTML = `<option value="" class='internationalization' data-key='module.label'></option>`;
        modules.forEach((module) => {
          const option = document.createElement("option");
          option.value = module;
          option.textContent = module;
          moduleSelect.appendChild(option);

          const selectedLang =
            document.getElementById("languageSelect")?.value || "pt";
          updateLanguageLabels(selectedLang);
        });
      })
      .catch((err) => {
        console.error("Erro ao buscar módulos:", err);
      });
  }

  function populateMainTable(ioList) {
    const tableBody = document.getElementById("validationTableBody");
    tableBody.innerHTML = "";

    ioList.forEach((io) => {
      const tr = document.createElement("tr");

      const td0 = createCellCustomIOState(io[1], io[1]);

      const tdEmpty1 = document.createElement("td");
      const tdEmpty2 = document.createElement("td");
      td0.classList.add("first-cell");

      tr.appendChild(td0);
      tr.appendChild(tdEmpty1);
      tr.appendChild(tdEmpty2);
      tr.appendChild(createCellCustom(io[2], io[1]));
      tr.appendChild(createCellCustom(io[3], io[1]));
      tr.appendChild(createCellCustom(io[4], io[1]));
      tr.appendChild(createCellCustom(io[5], io[1]));

      const detailImportBtnTd = document.createElement("td");

      const detailValidationBtnTd = document.createElement("td");

      const detailGenerationBtnTd = document.createElement("td");
      detailGenerationBtnTd.classList.add("last-cell");
      const btnImport = document.createElement("span");
      btnImport.innerHTML = `
          <span style="color: #0d6efd; cursor: pointer; text-decoration: underline;">
            <i class="bi bi-box-arrow-up-right"></i>
          </span>`;

      const btnValidation = document.createElement("span");
      btnValidation.innerHTML = `
          <span style="color: #0d6efd; cursor: pointer; text-decoration: underline;">
            <i class="bi bi-box-arrow-up-right"></i>
          </span>`;

      const btnGeneration = document.createElement("span");
      btnGeneration.innerHTML = `
          <span style="color: #0d6efd; cursor: pointer; text-decoration: underline;">
            <i class="bi bi-box-arrow-up-right"></i>
          </span>`;

      detailImportBtnTd.appendChild(btnImport);
      detailValidationBtnTd.appendChild(btnValidation);
      detailGenerationBtnTd.appendChild(btnGeneration);
      tr.appendChild(detailImportBtnTd);
      tr.appendChild(detailValidationBtnTd);
      tr.appendChild(detailGenerationBtnTd);
      btnImport.onclick = () => toggleValidationDetails(io[0], btnImport);
      btnValidation.onclick = () => toggleValidationDetails(io[0], btnValidation);
      btnGeneration.onclick = () => toggleValidationDetails(io[0], btnGeneration);
      detailImportBtnTd.style.backgroundColor = "lightGrey";
      detailValidationBtnTd.style.backgroundColor = "lightGrey";
      detailGenerationBtnTd.style.backgroundColor = "lightGrey";
      if (io[1] == 2) {
        detailImportBtnTd.style.backgroundColor = "#fff3cd";
        detailValidationBtnTd.style.backgroundColor = "#fff3cd";
        detailGenerationBtnTd.style.backgroundColor = "#fff3cd";
      } else if (io[1] == 3) {
        detailImportBtnTd.style.backgroundColor = "#f8d7da";
        detailValidationBtnTd.style.backgroundColor = "#f8d7da";
        detailGenerationBtnTd.style.backgroundColor = "#f8d7da";
      }
      tableBody.appendChild(tr);

      const detailImportRow = document.createElement("tr");
      const detailValidationRow = document.createElement("tr");
      const detailGenerationRow = document.createElement("tr");

      detailImportRow.style.display = "none";
      detailImportRow.className = "detail-row";

      detailValidationRow.style.display = "none";
      detailValidationRow.className = "detail-row";

      detailGenerationRow.style.display = "none";
      detailGenerationRow.className = "detail-row";

      const detailImportTd = document.createElement("td");
      detailImportTd.colSpan = 10;
      detailImportTd.style.padding = 10;
      const loadingImportDiv = document.createElement("div");
      loadingImportDiv.id = `detail-validation-${io[0]}`;
      loadingImportDiv.classList.add("internationalization");
      loadingImportDiv.setAttribute("data-key", "loading.label");
      detailImportTd.appendChild(loadingImportDiv);

      const detailValidationTd = document.createElement("td");
      detailValidationTd.colSpan = 10;
      detailValidationTd.style.padding = 10;
      const loadingValidationDiv = document.createElement("div");
      loadingValidationDiv.id = `detail-validation-${io[0]}`;
      loadingValidationDiv.classList.add("internationalization");
      loadingValidationDiv.setAttribute("data-key", "loading.label");
      detailValidationTd.appendChild(loadingValidationDiv);

      const detailGenerationTd = document.createElement("td");
      detailGenerationTd.colSpan = 10;
      detailGenerationTd.style.padding = 10;
      const loadingGenerationDiv = document.createElement("div");
      loadingGenerationDiv.id = `detail-validation-${io[0]}`;
      loadingGenerationDiv.classList.add("internationalization");
      loadingGenerationDiv.setAttribute("data-key", "loading.label");
      detailGenerationTd.appendChild(loadingGenerationDiv);

      detailImportRow.appendChild(detailImportTd);
      detailValidationRow.appendChild(detailValidationTd);
      detailGenerationRow.appendChild(detailGenerationTd);

      tableBody.appendChild(detailImportRow);
      tableBody.appendChild(detailValidationRow);
      tableBody.appendChild(detailGenerationRow);
    });
  }

  function renderRows(tbody, filteredDetails, currentPage, rowsPerPage) {
    tbody.innerHTML = "";

    const start = (currentPage - 1) * rowsPerPage;
    const end = start + rowsPerPage;
    const pageItems = filteredDetails.slice(start, end);

    pageItems.forEach((d) => {
      const bodyRow = document.createElement("tr");
      const resultado = d.resultado ?? "-";
      let rowStyle = "";

      if (resultado === "RULE OK") {
        rowStyle = `background-color: #d4edda`;
      } else if (resultado === "RULE DO NOT RUN") {
        rowStyle = `background-color: #fff3cd`;
      } else if (resultado === "RULE NOT OK") {
        rowStyle = `background-color: #f8d7da`;
      }

      const tdRuleCodeKey = document.createElement("td");
      tdRuleCodeKey.setAttribute("style", rowStyle);
      tdRuleCodeKey.textContent = d.regraCode ?? "-";
      tdRuleCodeKey.style.fontSize = "0.7rem";

      bodyRow.appendChild(tdRuleCodeKey);

      const severityKey = ((d.severity ?? "unknown") + "").toLowerCase();
      const tdSeverity = document.createElement("td");
      tdSeverity.setAttribute("style", rowStyle);

      const iconSeverity = document.createElement("i");
      iconSeverity.classList.add("bi", "me-2");

      let severityLabel = "";

      if (severityKey === "ok") {
        iconSeverity.classList.add("bi-check-circle-fill", "text-success");
        severityLabel = "success.label";
      } else if (severityKey === "warning") {
        iconSeverity.classList.add(
          "bi-exclamation-triangle-fill",
          "text-warning"
        );
        severityLabel = "warning.label";
      } else if (severityKey === "error") {
        iconSeverity.classList.add("bi-x-circle-fill", "text-danger");
        severityLabel = "error.label";
      }
      iconSeverity.classList.add("internationalization");
      iconSeverity.setAttribute("data-key", severityLabel);
      if (languageLabels[severityLabel]) {
        iconSeverity.title = languageLabels[severityLabel];
      }

      tdSeverity.appendChild(iconSeverity);
      bodyRow.appendChild(tdSeverity);

      const tdRuleDomainKey = document.createElement("td");
      tdRuleDomainKey.setAttribute("style", rowStyle);
      tdRuleDomainKey.textContent = d.regraDomain ?? "-";
      tdRuleDomainKey.style.fontSize = "0.7rem";
      tdRuleDomainKey.style.overflowWrap = "break-word";
      bodyRow.appendChild(tdRuleDomainKey);

      const tdRule = document.createElement("td");
      tdRule.textContent = d.regra ?? "-";
      tdRule.classList.add("firstItemDetails");
      tdRule.setAttribute("style", rowStyle);
      tdRule.style.fontSize = "0.7rem";
      bodyRow.appendChild(tdRule);

      const tdRuleValuesKey = document.createElement("td");
      tdRuleValuesKey.setAttribute("style", rowStyle);
      tdRuleValuesKey.textContent = d.regraExecutada ?? "-";
      tdRuleValuesKey.style.fontSize = "0.7rem";
      bodyRow.appendChild(tdRuleValuesKey);

      const tdResult = document.createElement("td");
      tdResult.classList.add("internationalization");
      tdResult.setAttribute(
        "data-key",
        resultado === "RULE OK"
          ? "ruleOK.label"
          : resultado === "RULE DO NOT RUN"
          ? "ruleSkip.label"
          : resultado === "RULE NOT OK"
          ? "ruleNotOK.label"
          : resultado === "RULE DO NOT RUN PREREQUISITE"
          ? "ruleSkipPre.label"
          : resultado === "RULE OK WITH NOT OK"
          ? "ruleOKNotOK.label"
          : ""
      );
      tdResult.setAttribute("style", rowStyle);
      tdResult.textContent = resultado;
      tdResult.setAttribute("data-raw", resultado.toLowerCase());
      tdResult.style.fontSize = "0.7rem";
      bodyRow.appendChild(tdResult);

      const tdProcessDate = document.createElement("td");
      tdProcessDate.textContent = d.dataProcessamento ?? "-";
      tdProcessDate.classList.add("lastItemDetails");
      tdProcessDate.setAttribute("style", rowStyle);
      tdProcessDate.style.fontSize = "0.7rem";
      bodyRow.appendChild(tdProcessDate);

      tbody.appendChild(bodyRow);
    });
  }

  const pageStates = {};

  function renderPagination(container, renderPageCallback) {
    const totalPages = Math.ceil(currentDetailFilteredData.length / currentDetailRowsPerPage);
    container.innerHTML = "";

    if (totalPages <= 1) return;

    const selectedLang = document.getElementById("languageSelect")?.value || "pt";

    const makeBtn = (label, page = null, { active = false, disabled = false, extraClass = "" } = {}) => {
      const btn = document.createElement("button");
      btn.textContent = label;
      btn.className = `page-btn btn btn-sm btn-light m-1 roundButton ${extraClass}`;
      if (active) btn.classList.add("active");
      btn.disabled = !!disabled;

      if (!disabled && page !== null) {
        btn.addEventListener("click", () => {
          currentDetailCurrentPage = page;
          updateLanguageLabels(selectedLang); 
          renderPageCallback();
        });
      }
      return btn;
    };

    container.appendChild(
      makeBtn("«", currentDetailCurrentPage > 1 ? currentDetailCurrentPage - 1 : null, {
        disabled: currentDetailCurrentPage === 1,
      })
    );

    const start = Math.max(1, currentDetailCurrentPage - 1);
    const end = Math.min(totalPages, currentDetailCurrentPage + 3);

    for (let p = start; p <= end; p++) {
      container.appendChild(
        makeBtn(String(p), p, { active: p === currentDetailCurrentPage })
      );
    }

    if (end < totalPages) {
      if (end + 1 < totalPages) {
        container.appendChild(
          makeBtn("…", null, { disabled: true})
        );
      }
      container.appendChild(
        makeBtn(String(totalPages), totalPages, { active: currentDetailCurrentPage === totalPages })
      );
    }

    container.appendChild(
      makeBtn("»", currentDetailCurrentPage < totalPages ? currentDetailCurrentPage + 1 : null, {
        disabled: currentDetailCurrentPage === totalPages,
      })
    );
  }

  function toggleImportDetails (ioid, button){
    let fetchedDetails =  [];

    const row = button.closest("tr").nextElementSibling;
    const container = document.getElementById(`detail-import-${ioid}`);

    const isHidden = row.style.display === "none";

    row.style.display = isHidden ? "table-row" : "none";

    if (isHidden) {
      document.getElementById("upload-area")?.classList.add("collapsed");
      document.getElementById("paginationDiv").style.display = "block";
    } else {
      document.getElementById("upload-area")?.classList.remove("collapsed");
      currentOpenDetailIOId = null;
      currentDetailFilteredData = [];
      document.getElementById("paginationDiv").style.display = "none";
    }

    const span = button.querySelector("span");

    span.innerHTML = "";

    const icon = document.createElement("i");
    icon.className = "bi bi-box-arrow-up-right";
    icon.style.marginLeft = "6px";

    span.appendChild(labelText);
    span.appendChild(icon);

    const selectedLang =
      document.getElementById("languageSelect")?.value || "pt";
    updateLanguageLabels(selectedLang);
    
    const tbody = document.createElement("tbody");

    if (isHidden && !container.dataset.loaded) {
      fetch(`/importFile/import/results/${ioid}`)
        .then((res) => res.json())
        .then((details) => {
          currentOpenDetailIOId = ioid;
          fetchedDetails = details;
          currentDetailFilteredData = [...fetchedDetails];
          currentDetailCurrentPage = 1;

          const doRender = () => {
            renderRows(tbody, currentDetailFilteredData, currentDetailCurrentPage, currentDetailRowsPerPage);
            renderPagination(document.getElementById("sharedPaginationContainer"), doRender);
          };

          const scrollContainer = document.createElement("div");
          scrollContainer.className = "detailsTable";
          scrollContainer.classList.add("detailsTable");
          const table = document.createElement("table");
          table.className = "fixed-header-table table table-borderless";

          const thead = document.createElement("thead");
          thead.style.zIndex = 4;
          const headerRow = document.createElement("tr");

          const header = [
            { key: "module.label", className: "firstItemDetails" },
            { key: "entity.label" },
            { key: "domain.label" },
            { key: "referenceDate.label" },
            { key: "description.label" },
            { key: "date.label", className: "lastItemDetails" },
          ];

          header.forEach(({ key, className }) => {
            const th = document.createElement("th");
            th.classList.add("internationalization");
            th.setAttribute("data-key", key);
            th.style.fontSize = "0.9rem";
            if (className) {
              th.classList.add(className);
            }
            headerRow.appendChild(th);
          });

          const filterContainer = document.createElement("div");
          filterContainer.className = "resultado-import-filter-group";

          doRender();

          thead.appendChild(headerRow);
          table.appendChild(thead);
            details.forEach((d) => {
              const bodyRow = document.createElement("tr");
              const tdModuleCodeKey = document.createElement("td");
              tdModuleCodeKey.setAttribute("style", rowStyle);
              tdModuleCodeKey.textContent = d.code ?? "-";
              tdModuleCodeKey.style.fontSize = "0.7rem";

              bodyRow.appendChild(tdModuleCodeKey);

              const tdEntityCodeKey = document.createElement("td");
              tdEntityCodeKey.setAttribute("style", rowStyle);
              tdEntityCodeKey.textContent = d.entity ?? "-";
              tdEntityCodeKey.style.fontSize = "0.7rem";

              bodyRow.appendChild(tdEntityCodeKey);

              const tdDomainCodeKey = document.createElement("td");
              tdDomainCodeKey.setAttribute("style", rowStyle);
              tdDomainCodeKey.textContent = d.domain ?? "-";
              tdDomainCodeKey.style.fontSize = "0.7rem";

              bodyRow.appendChild(tdDomainCodeKey);

              const tdReferenceDateCodeKey = document.createElement("td");
              tdReferenceDateCodeKey.setAttribute("style", rowStyle);
              tdReferenceDateCodeKey.textContent = d.referenceDate ?? "-";
              tdReferenceDateCodeKey.style.fontSize = "0.7rem";
              bodyRow.appendChild(tdReferenceDateCodeKey);

              const tdDescriptionCodeKey = document.createElement("td");
              tdDescriptionCodeKey.textContent = d.description ?? "-";
              tdDescriptionCodeKey.setAttribute("style", rowStyle);
              tdDescriptionCodeKey.style.fontSize = "0.7rem";
              bodyRow.appendChild(tdDescriptionCodeKey);

              const tdTimestampCodeKey = document.createElement("td");
              tdTimestampCodeKey.setAttribute("style", rowStyle);
              tdTimestampCodeKey.textContent = d.timestamp ?? "-";
              tdTimestampCodeKey.style.fontSize = "0.7rem";
              bodyRow.appendChild(tdTimestampCodeKey);

              tbody.appendChild(bodyRow);
            });

            table.appendChild(tbody);

            scrollContainer.appendChild(filterContainer);
            scrollContainer.appendChild(table);

          

            container.innerHTML = "";
            container.classList.remove("internationalization");
            container.removeAttribute("data-key");


            container.appendChild(scrollContainer);

            container.dataset.loaded = "true";

            const selectedLang =
              document.getElementById("languageSelect")?.value || "pt";
            updateLanguageLabels(selectedLang);
        })
        .catch((err) => {
          container.textContent =
            languageLabels["errorDetails.label"] || "Error loading details.";
          container.classList.add("internationalization");
          container.setAttribute("data-key", "errorDetails.label");

          console.error(err);
        });
    }
  }

  function toggleValidationDetails(ioid, button) {
    let fetchedDetails =  [];

    const row = button.closest("tr").nextElementSibling;
    const container = document.getElementById(`detail-validation-${ioid}`);

    const isHidden = row.style.display === "none";

    row.style.display = isHidden ? "table-row" : "none";

    if (isHidden) {
      document.getElementById("upload-area")?.classList.add("collapsed");
      document.getElementById("paginationDiv").style.display = "block";
    } else {
      document.getElementById("upload-area")?.classList.remove("collapsed");
      currentOpenDetailIOId = null;
      currentDetailFilteredData = [];
      document.getElementById("paginationDiv").style.display = "none";
    }

    const span = button.querySelector("span");

    span.innerHTML = "";

    const icon = document.createElement("i");
    icon.className = "bi bi-box-arrow-up-right";
    icon.style.marginLeft = "6px";

    span.appendChild(icon);

    const selectedLang =
      document.getElementById("languageSelect")?.value || "pt";
    updateLanguageLabels(selectedLang);
    
    const tbody = document.createElement("tbody");

    if (isHidden && !container.dataset.loaded) {
      fetch(`/importFile/validation/results/${ioid}`)
        .then((res) => res.json())
        .then((details) => {
          currentOpenDetailIOId = ioid;
          fetchedDetails = details;
          currentDetailFilteredData = [...fetchedDetails];
          currentDetailCurrentPage = 1;

          const doRender = () => {
            renderRows(tbody, currentDetailFilteredData, currentDetailCurrentPage, currentDetailRowsPerPage);
            renderPagination(document.getElementById("sharedPaginationContainer"), doRender);
          };

          const scrollContainer = document.createElement("div");
          scrollContainer.className = "detailsTable";
          scrollContainer.classList.add("detailsTable");
          const table = document.createElement("table");
          table.className = "fixed-header-table table table-borderless";

          const thead = document.createElement("thead");
          thead.style.zIndex = 4;
          const headerRow = document.createElement("tr");

          const header = [
            { key: "ruleCode.label", className: "firstItemDetails" },
            { key: "severity.label" },
            { key: "ruleDomain.label" },
            { key: "rule.label" },
            { key: "ruleValues.label" },
            { key: "result.label" },
            { key: "processDate.label", className: "lastItemDetails" },
          ];

          header.forEach(({ key, className }) => {
            const th = document.createElement("th");
            th.classList.add("internationalization");
            th.setAttribute("data-key", key);
            th.style.fontSize = "0.9rem";
            if (className) {
              th.classList.add(className);
            }
            headerRow.appendChild(th);
          });
          const resultadoCounts = {};

          details.forEach((d) => {
            const key = d.resultado ?? "-";
            resultadoCounts[key] = (resultadoCounts[key] || 0) + 1;
          });

          const uniqueResultados = [
            ...new Set(details.map((d) => d.resultado ?? "-")),
          ];

          const labelKeyMap = {
            "RULE OK": "ruleOK",
            "RULE NOT OK": "ruleNotOK",
            "RULE DO NOT RUN": "ruleSkip",
            "RULE DO NOT RUN PREREQUISITE": "ruleSkipPre",
            "RULE OK WITH NOT OK": "ruleOKNotOK",
          };

          const filterContainer = document.createElement("div");
          filterContainer.className = "resultado-filter-group";

          const totalCount = details.length;

          const allButton = document.createElement("div");
          allButton.className =
            "resultado-filter-button active internationalization";
          allButton.setAttribute("data-key", "all.label");
          allButton.setAttribute("data-value", "__all__");
          allButton.setAttribute("data-count", totalCount);
          allButton.textContent = `All (${totalCount})`;
          filterContainer.appendChild(allButton);
          
          uniqueResultados.forEach((value) => {
            const keyBase = labelKeyMap[value] || "";
            const count = resultadoCounts[value] ?? 0;

            const labelText = languageLabels[`${keyBase}.label`] || value;
            const tooltipText =
              languageLabels[`${keyBase}.tooltip`] || labelText;

            const button = document.createElement("div");
            button.className = "resultado-filter-button internationalization";
            button.setAttribute("data-key", `${keyBase}.label`);
            button.setAttribute("data-tooltip-key", `${keyBase}.tooltip`);
            button.setAttribute("data-value", value);
            button.setAttribute("data-count", count);
            button.setAttribute("data-bs-toggle", "tooltip");
            button.setAttribute("data-bs-placement", "top");
            button.textContent = count ? `${labelText} (${count})` : labelText;
            filterContainer.appendChild(button);
          });

          filterContainer.querySelectorAll(".resultado-filter-button").forEach(button => {
            button.addEventListener("click", function () {
              const selected = this.getAttribute("data-value").toLowerCase();

              filterContainer.querySelectorAll(".resultado-filter-button").forEach(btn => btn.classList.remove("active"));
              this.classList.add("active");

              currentDetailFilteredData = details.filter(d => {
                const result = (d.resultado ?? "").toLowerCase();
                return selected === "__all__" || result === selected;
              });

              currentDetailCurrentPage = 1;

              doRender();

              const selectedLang = document.getElementById("languageSelect")?.value || "pt";
              updateLanguageLabels(selectedLang);
            });
          });

          doRender();

          reinitTooltips(filterContainer);

          thead.appendChild(headerRow);
          table.appendChild(thead);

          doRender();
          table.appendChild(tbody);

          scrollContainer.appendChild(filterContainer);
          scrollContainer.appendChild(table);

          

          container.innerHTML = "";
          container.classList.remove("internationalization");
          container.removeAttribute("data-key");


          container.appendChild(scrollContainer);

          container.dataset.loaded = "true";

          const selectedLang =
            document.getElementById("languageSelect")?.value || "pt";
          updateLanguageLabels(selectedLang);
        })
        .catch((err) => {
          container.textContent =
            languageLabels["errorDetails.label"] || "Error loading details.";
          container.classList.add("internationalization");
          container.setAttribute("data-key", "errorDetails.label");

          console.error(err);
        });
    }
  }

  function createCell(value) {
    const td = document.createElement("td");
    td.textContent = value ?? "-";
    return td;
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

  function autoSubmit() {
    const moduleFilter = document
      .getElementById("filterModule")
      .value.trim()
      .toLowerCase();

    const selectedYear = (document.getElementsByName("filterYear")[0].value || "").trim();
    const selectedMonthRaw = document.getElementById("filterMonth").value || "";
    const selectedMonth = selectedMonthRaw ? String(selectedMonthRaw).padStart(2, "0") : "";

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
