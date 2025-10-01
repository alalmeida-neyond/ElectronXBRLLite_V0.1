function downloadMetadata(event) {
    event.preventDefault();

    const form = document.getElementById("metadataForm");
    const formData = new FormData(form);

    const uploadButton = document.getElementById("uploadButtonMetadata");
    const loadingButton = document.getElementById("loadingButton");
    const alertUpdate = document.getElementById("alert-success");

    uploadButton.style.display = "none";
    loadingButton.style.display = "inline-block";

    fetch("/processMetaData", {
        method: "POST",
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.enabled) {
            uploadButton.style.display = "inline-block";
        }
    })
    .catch(error => {
        console.error("Error:", error);
    })
    .finally(() => {
        alertUpdate.style.display = "block";
        uploadButton.style.display = "inline-block";
        loadingButton.style.display = "none";
    });
}



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
                if (value && typeof translation === "string") {
                    translation = translation.replace("{0}", value);
                }

                if (!translation) return;

                if (element.tagName === "INPUT") {
                    const type = element.getAttribute("type")?.toLowerCase();
                    if (type === "submit" || type === "button") {
                        element.value = translation;
                    } else {
                        element.placeholder = translation;
                    }
                } else if (element.tagName === "I") {
                    element.title = translation;
                } else {
                    // Set once; avoid duplicating text nodes
                    element.textContent = translation;
                }
            });
        })
        .catch(error => {
            console.error("Erro ao carregar idioma:", error);
        });
}

// (legacy, still used by the <input webkitdirectory> fallback)
function selectFolder() {
    const folderInput = document.getElementById('folderInput');
    const directoryDisplay = document.getElementById('directoryDisplay');

    if (folderInput.files.length > 0) {
        const firstFile = folderInput.files[0];
        const folderPath = firstFile.webkitRelativePath.split('/')[0];

        if (directoryDisplay) {
            directoryDisplay.innerHTML = `${folderPath}`;
            directoryDisplay.style.display = 'block';
        }
    }
}

function openFolderSelector() {
    document.getElementById('folderInput').click();
}

// Universal chooser that works in Electron and browsers
async function chooseFolderAndPersist() {
    const folderPathTextarea = document.getElementById('folderPath');

    // A) Electron via preload bridge
    if (window.api && typeof window.api.selectFolder === 'function') {
        try {
            const folderPath = await window.api.selectFolder();
            if (folderPath) {
                folderPathTextarea.value = folderPath;
            }
            return;
        } catch (e) {
            console.error('Electron selectFolder failed:', e);
            // fallthrough to browser strategies
        }
    }

    if (typeof window.showDirectoryPicker === 'function') {
        try {
            const dirHandle = await window.showDirectoryPicker();

            // Optional: write a small marker file into the chosen folder
            try {
                const fileHandle = await dirHandle.getFileHandle('path.dat', { create: true });
                const writable = await fileHandle.createWritable();
                await writable.write(`${dirHandle.name}`);
                await writable.close();
            } catch (writeErr) {
                console.warn('Could not write path.dat (permission or user canceled):', writeErr);
            }

            folderPathTextarea.value = `[Browser] ${dirHandle.name}`;
            return;
        } catch (e) {
            console.warn('Directory picker canceled or failed:', e);
        }
    }

    const input = document.getElementById('folderInput'); // ensure it exists in HTML
    input.onchange = () => {
        if (!input.files || input.files.length === 0) return;
        const first = input.files[0];
        const top = (first.webkitRelativePath || '').split('/')[0] || '(folder)';
        folderPathTextarea.value = `[Browser] ${top}`;
    };
    input.click();
}

document.addEventListener("DOMContentLoaded", function () {
    const button = document.getElementById('chooseFolder');
    if (button) {
        // Always enabled; handler picks the right strategy at runtime
        button.disabled = false;
        button.addEventListener('click', (e) => {
            e.preventDefault();
            chooseFolderAndPersist();
        });
    }

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

    const folderInput = document.getElementById('folderInput');
    if (folderInput) {
        folderInput.addEventListener('change', selectFolder);
    }
});
