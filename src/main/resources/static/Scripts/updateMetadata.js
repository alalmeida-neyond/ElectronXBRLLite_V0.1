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
                    element.textContent = translation;
                }
            });
        })
        .catch(error => {
            console.error("Erro ao carregar idioma:", error);
        });
}

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

async function chooseFolderAndPersist() {
    const folderPathTextarea = document.getElementById('folderPath');

    if (window.api && typeof window.api.selectFolder === 'function') {
        try {
            const folderPath = await window.api.selectFolder();
            if (folderPath) {
                folderPathTextarea.value = folderPath;
            }
            return;
        } catch (e) {
            console.error('Electron selectFolder failed:', e);
        }
    }
    if (typeof window.showDirectoryPicker === 'function') {
        try {
            const dirHandle = await window.showDirectoryPicker();

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

    const input = document.getElementById('folderInput');
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
        button.disabled = false;
        button.addEventListener('click', (e) => {
            e.preventDefault();
            chooseFolderAndPersist();
        });
    }

    const langSelect = document.getElementById("languageSelect");
    if (langSelect) {
        langSelect.value = window.I18N.get();
        langSelect.addEventListener("change", function () {
            window.I18N.set(langSelect.value);
            const selectedLang = langSelect.value;
            updateLanguageLabels(selectedLang);
        });
        updateLanguageLabels(window.I18N.get());
    } else {
        console.warn("Elemento #languageSelect não encontrado");
        updateLanguageLabels(window.I18N.get());
    }

    const folderInput = document.getElementById('folderInput');
    if (folderInput) {
        folderInput.addEventListener('change', selectFolder);
    }
});
