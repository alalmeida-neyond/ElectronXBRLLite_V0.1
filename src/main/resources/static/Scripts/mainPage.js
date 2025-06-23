
let languageLabels = {};
function updateLanguageLabels(language) {
    console.log("Idioma selecionado:", language);

    fetch(`./Languages_Files/${language}.json`)
        .then(response => {
            if (!response.ok) throw new Error("Ficheiro de idioma não encontrado");
            return response.json();
        })
        .then(data => {
            document.querySelectorAll(".internationalization").forEach(element => {
                const key = element.getAttribute("data-key");
                if (data[key]) {
                    if (element.tagName === "INPUT") {
                        element.placeholder = data[key];
                    } else {
                        element.textContent = data[key];
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
            .then(response => response.text())
            .then(data => {
                document.getElementById("logContainer").style.display = "block";
                document.getElementById("upload-text").style.display = "block";
                document.getElementById("loading-text").style.display = "none";
                fetchModulesFromBackend();
                fetchIOs();
            })
            .catch(error => {
                console.error("Erro no upload:", error);
                document.getElementById("upload-text").style.display = "block";
                document.getElementById("loading-text").style.display = "none";
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

        let iconClass = "";
        let text = value ?? "-";
        const image = document.createElement("i");

        image.classList.add("bi");
        image.classList.add("me-2");
        if (ioStateId == 1) {
            image.classList.add("bi-check-circle-fill");
            image.classList.add("text-success");


            td.setAttribute("data-key", "ok.label");
            td.style.backgroundColor = "lightGrey";
        } else if (ioStateId == 2) {

            image.classList.add("bi-exclamation-triangle-fill");
            image.classList.add("text-warning");

            td.setAttribute("data-key", "warning.label");
            td.style.backgroundColor = "#fff3cd";
        } else if (ioStateId == 3) {

            image.classList.add("bi-x-circle-fill");
            image.classList.add("text-danger");

            td.setAttribute("data-key", "error.label");
            td.style.backgroundColor = "#f8d7da";
        }
        td.appendChild(image);
        td.appendChild(document.createTextNode(text));
        td.classList.add("first-cell");
        td.classList.add("internationalization");
        return td;
    }

    function createCellCustom(value, ioStateId) {
        const td = document.createElement("td");

        //td.classList.add("ioCell");

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

            const imageDetails = document.createElement("i");
            imageDetails.classList.add("bi", "bi-box-arrow-up-right");
            imageDetails.style.color = "#0d6efd";
            imageDetails.style.cursor = "pointer";
            imageDetails.style.textDecoration = "underline";
            btn.appendChild(imageDetails);
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
                        { key: "rule.label", className: "firstItemDetails" },
                        { key: "severity.label" },
                        { key: "result.label" },
                        { key: "processDate.label", className: "lastItemDetails" }
                    ];

                    header.forEach(({ key, className }) => {
                        const th = document.createElement("th");
                        th.classList.add("internationalization");
                        th.setAttribute("data-key", key);
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

                        const tdRule = document.createElement("td");
                        tdRule.textContent = d.regra ?? "-";
                        tdRule.classList.add("firstItemDetails");
                        tdRule.setAttribute("style", rowStyle);
                        bodyRow.appendChild(tdRule);

                        const severityKey = ((d.severity ?? 'unknown') + '').toLowerCase() + ".label";
                        const tdSeverity = document.createElement("td");
                        tdSeverity.classList.add("internationalization");
                        tdSeverity.setAttribute("data-key", severityKey);
                        tdSeverity.setAttribute("style", rowStyle);
                        tdSeverity.textContent = d.severity ?? "-";

                        bodyRow.appendChild(tdSeverity);

                        const tdResult = document.createElement("td");
                        tdResult.classList.add("internationalization");
                        tdResult.setAttribute("data-key",
                            resultado === "RULE OK" ? "ruleOK.label" :
                                resultado === "RULE DO NOT RUN" ? "ruleSkip.label" :
                                    resultado === "RULE NOT OK" ? "ruleNotOK.label" : ""
                        );
                        tdResult.setAttribute("style", rowStyle);
                        tdResult.textContent = resultado;
                        bodyRow.appendChild(tdResult);

                        const tdProcessDate = document.createElement("td");
                        tdProcessDate.classList.add("internationalization");
                        tdProcessDate.textContent = d.dataProcessamento ?? "-";
                        tdProcessDate.classList.add("lastItemDetails");
                        tdProcessDate.setAttribute("style", rowStyle);
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

    // On page load, optionally fetch existing IOs
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

