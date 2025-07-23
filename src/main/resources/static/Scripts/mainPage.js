
let languageLabels = {};
function updateLanguageLabels(language) {
    fetch(`./Languages_Files/${language}.json`)
        .then(response => {
            if (!response.ok) throw new Error("Ficheiro de idioma não encontrado");
            return response.json();
        })
        .then(data => {
            document.querySelectorAll(".internationalization").forEach(element => {
                const key = element.getAttribute("data-key");
                let translation = data[key];
                
                const value = element.getAttribute("data-value");
                if (value) {
                    translation = translation.replace("{0}", value);
                }
                element.textContent = translation;

                if (translation) {
                    if (element.tagName === "INPUT") {
                        element.placeholder = translation;
                    } else if (element.tagName === "I"){
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

        })
        .catch(error => {
            console.error("Erro ao carregar idioma:", error);
        });
}
document.addEventListener("DOMContentLoaded", function () {
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

    ["filterYear", "filterMonth", "filterDay"].forEach(id => {
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

    document.getElementById("filterModule").addEventListener("change", autoSubmit);

    function uploadFile(file) {
        const formData = new FormData();
        formData.append("file", file);

        document.getElementById("upload-text").style.display = "none";
        document.getElementById("loading-text").style.display = "block";

        fetch("/importFile/upload", {
            method: "POST",
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.text();
            })
            .then(data => {
                document.getElementById("logContainer").style.display = "block";
                document.getElementById("upload-text").style.display = "block";
                document.getElementById("loading-text").style.display = "none";
                document.getElementById("upload-error").style.display = "none";
                fetchModulesFromBackend();
                fetchIOs();
            })
            .catch(error => {
                console.error("Erro ao fazer upload:", error.message); 
                document.getElementById("upload-text").style.display = "block";
                document.getElementById("loading-text").style.display = "none";

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
            .then(res => {
                if (res.ok != true) {
                    throw new Error("Erro ao obter os IOs");
                }
                return res.json();
            })
            .then(data => {
                allIOs = data;
                populateMainTable(allIOs);
            })
            .catch(err => {
                console.error("Erro ao buscar os IOs:", err);
            });
    }

    function createCellCustomIOState(value, ioStateId) {
        const td = document.createElement("td");

        const icon = document.createElement("i");
        icon.classList.add("bi", "me-2");

        let label = "";
        let backgroundColor = "";

        if (ioStateId == 1) {
            icon.classList.add("bi-check-circle-fill", "text-success");
            label = "ok.label";
            backgroundColor = "lightgrey";
        } else if (ioStateId == 2) {
            icon.classList.add("bi-exclamation-triangle-fill", "text-warning");
            label = "warning.label";
            backgroundColor = "#fff3cd";
        } else if (ioStateId == 3) {
            icon.classList.add("bi-x-circle-fill", "text-danger");
            label = "error.label";
            backgroundColor = "#f8d7da";
        }

        td.appendChild(icon);

        td.setAttribute("data-key", label);
        td.classList.add("first-cell", "internationalization");
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
        }
        else {
            td.style.backgroundColor = "lightGrey";
        }

        td.textContent = text;
        return td;
    }

    function fetchModulesFromBackend() {
        const moduleSelect = document.getElementById("filterModule");
        fetch("/importFile/modules")
            .then(res => res.json())
            .then(modules => {
                moduleSelect.innerHTML = `<option value="" class='internationalization' data-key='module.label'></option>`;
                modules.forEach(module => {
                    const option = document.createElement("option");
                    option.value = module;
                    option.textContent = module;
                    moduleSelect.appendChild(option);

                    const selectedLang = document.getElementById("languageSelect")?.value || "pt";
                    updateLanguageLabels(selectedLang);
                });
            })
            .catch(err => {
                console.error("Erro ao buscar módulos:", err);
            });
    }

    function populateMainTable(ioList) {
        const tableBody = document.getElementById("validationTableBody");
        tableBody.innerHTML = "";

        ioList.forEach(io => {
            const tr = document.createElement("tr");

            const td0 = createCellCustomIOState(io[1], io[1]);

            tr.appendChild(td0);
            tr.appendChild(createCellCustom(io[2], io[1]));
            tr.appendChild(createCellCustom(io[3], io[1]));
            tr.appendChild(createCellCustom(io[4], io[1]));
            tr.appendChild(createCellCustom(io[5], io[1]));

            const detailBtnTd = document.createElement("td");
            detailBtnTd.classList.add("last-cell");
            const btn = document.createElement("span");
            btn.innerHTML = `
          <span class= 'internationalization' data-key='details.label' style="color: #0d6efd; cursor: pointer; text-decoration: underline;">
            <i class="bi bi-box-arrow-up-right"></i>
          </span>`;

            detailBtnTd.appendChild(btn);
            tr.appendChild(detailBtnTd);
            btn.onclick = () => toggleDetails(io[0], btn);
            detailBtnTd.style.backgroundColor = "lightGrey";
            if (io[1] == 2) {
                detailBtnTd.style.backgroundColor = "#fff3cd";
            }
            else if (io[1] == 3) {
                detailBtnTd.style.backgroundColor = "#f8d7da";
            }
            tableBody.appendChild(tr);

            const detailRow = document.createElement("tr");

            detailRow.style.display = "none";
            detailRow.className = "detail-row";

            const detailTd = document.createElement("td");
            detailTd.colSpan = 7;
            detailTd.style.padding = 10;
            const loadingDiv = document.createElement("div");
            loadingDiv.id = `detail-${io[0]}`;
            loadingDiv.classList.add("internationalization");
            loadingDiv.setAttribute("data-key", "loading.label");
            detailTd.appendChild(loadingDiv);

            detailRow.appendChild(detailTd);

            tableBody.appendChild(detailRow);

        });
    }

    function toggleDetails(ioid, button) {
        const row = button.closest("tr").nextElementSibling;
        const container = document.getElementById(`detail-${ioid}`);

        const isHidden = row.style.display === "none";

        row.style.display = isHidden ? "table-row" : "none";



        const span = button.querySelector("span");

        const newKey = isHidden ? "retreat.label" : "details.label";
        span.setAttribute("data-key", newKey);
        span.classList.add("internationalization");

        span.innerHTML = "";

        const labelText = document.createTextNode(languageLabels[newKey] || (isHidden ? "Recolher" : "Detalhes"));
        const icon = document.createElement("i");
        icon.className = "bi bi-box-arrow-up-right";
        icon.style.marginLeft = "6px";

        span.appendChild(labelText);
        span.appendChild(icon);

        const selectedLang = document.getElementById("languageSelect")?.value || "pt";
        updateLanguageLabels(selectedLang);



        if (isHidden && !container.dataset.loaded) {
            fetch(`/importFile/results/${ioid}`)
                .then(res => res.json())
                .then(details => {
                    const table = document.createElement("table");
                    table.className = "fixed-header-table table table-borderless";

                    const thead = document.createElement("thead");
                    const headerRow = document.createElement("tr");

                    const header = [
                        { key: "ruleCode.label", className: "firstItemDetails" },
                        { key: "severity.label" },
                        { key: "ruleDomain.label" },
                        { key: "rule.label"},
                        { key: "ruleValues.label"},
                        { key: "result.label" },
                        { key: "processDate.label", className: "lastItemDetails" }
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
                    thead.appendChild(headerRow);
                    table.appendChild(thead);

                    const tbody = document.createElement("tbody");
                    details.forEach(d => {
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

                        const ruleCodeKey = ((d.regraCode ?? 'unknown') + '').toLowerCase() + ".label";
                        const tdRuleCodeKey = document.createElement("td");
                        tdRuleCodeKey.classList.add("internationalization");
                        tdRuleCodeKey.setAttribute("data-key", ruleCodeKey);
                        tdRuleCodeKey.setAttribute("style", rowStyle);
                        tdRuleCodeKey.textContent = d.regraCode ?? "-";
                        tdRuleCodeKey.style.fontSize = "0.7rem";

                        bodyRow.appendChild(tdRuleCodeKey);

                        const severityKey = ((d.severity ?? 'unknown') + '').toLowerCase();
                        const tdSeverity = document.createElement("td");
                        tdSeverity.setAttribute("style", rowStyle);

                        const iconSeverity = document.createElement("i");
                        iconSeverity.classList.add("bi", "me-2");

                        let severityLabel = "";

                        if (severityKey === "ok") {
                            iconSeverity.classList.add("bi-check-circle-fill", "text-success");
                            severityLabel = "success.label";
                        } else if (severityKey === "warning") {
                            iconSeverity.classList.add("bi-exclamation-triangle-fill", "text-warning");
                            severityLabel = "warning.label";
                        } else if (severityKey === "error") {
                            iconSeverity.classList.add("bi-x-circle-fill", "text-danger");
                            severityLabel = "error.label";
                        }
                        iconSeverity.classList.add("internationalization");
                        iconSeverity.setAttribute("data-key", severityLabel);
                        if(languageLabels[severityLabel])
                        {
                            iconSeverity.title = languageLabels[severityLabel];
                        }
                        
                        
                        tdSeverity.appendChild(iconSeverity);
                        bodyRow.appendChild(tdSeverity);

                        const ruleDomainKey = ((d.regraDomain ?? 'unknown') + '').toLowerCase() + ".label";
                        const tdRuleDomainKey = document.createElement("td");
                        tdRuleDomainKey.classList.add("internationalization");
                        tdRuleDomainKey.setAttribute("data-key", ruleDomainKey);
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

                        const ruleValuesKey = ((d.regraExecutada ?? 'unknown') + '').toLowerCase() + ".label";
                        const tdRuleValuesKey = document.createElement("td");
                        tdRuleValuesKey.classList.add("internationalization");
                        tdRuleValuesKey.setAttribute("data-key", ruleValuesKey);
                        tdRuleValuesKey.setAttribute("style", rowStyle);
                        tdRuleValuesKey.textContent = d.regraExecutada ?? "-";
                        tdRuleValuesKey.style.fontSize = "0.7rem";
                        bodyRow.appendChild(tdRuleValuesKey);

                        const tdResult = document.createElement("td");
                        tdResult.classList.add("internationalization");
                        tdResult.setAttribute("data-key",
                            resultado === "RULE OK" ? "ruleOK.label" :
                                resultado === "RULE DO NOT RUN" ? "ruleSkip.label" :
                                    resultado === "RULE NOT OK" ? "ruleNotOK.label" : ""
                        );
                        tdResult.setAttribute("style", rowStyle);
                        tdResult.textContent = resultado;
                        tdResult.style.fontSize = "0.7rem";
                        bodyRow.appendChild(tdResult);

                        const tdProcessDate = document.createElement("td");
                        tdProcessDate.classList.add("internationalization");
                        tdProcessDate.textContent = d.dataProcessamento ?? "-";
                        tdProcessDate.classList.add("lastItemDetails");
                        tdProcessDate.setAttribute("style", rowStyle);
                        tdProcessDate.style.fontSize = "0.7rem";
                        bodyRow.appendChild(tdProcessDate);

                        tbody.appendChild(bodyRow);
                    });

                    table.appendChild(tbody);

                    container.innerHTML = "";
                    container.classList.remove("internationalization");
                    container.removeAttribute("data-key");
                    container.appendChild(table);
                    container.dataset.loaded = "true";


                    const selectedLang = document.getElementById("languageSelect")?.value || "pt";
                    updateLanguageLabels(selectedLang);
                })
                .catch(err => {
                    container.textContent = languageLabels["errorDetails.label"] || "Error loading details.";
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

    function autoSubmit() {
        const moduleFilter = document.getElementById("filterModule").value.trim().toLowerCase();

        const year = document.getElementById("filterYear").value;
        const month = document.getElementById("filterMonth").value.padStart(2, "0");
        const day = document.getElementById("filterDay").value.padStart(2, "0");

        const fullDate = (year && month && day) ? `${year}-${month}-${day}` : "";

        const filtered = allIOs.filter(io => {
            const moduleValue = (io[2] ?? "").toLowerCase().trim();
            const dateValue = (io[5] ?? "").trim();

            const moduleMatch = !moduleFilter || moduleValue.includes(moduleFilter);
            const dateMatch = !fullDate || dateValue.startsWith(fullDate);

            return moduleMatch && dateMatch;
        });

        populateMainTable(filtered);
    }

});

