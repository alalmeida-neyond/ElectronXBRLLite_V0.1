function downloadMetadata(event) {
    event.preventDefault();
    
    var formData = new FormData(document.getElementById("metadataForm"));
    let uploadButton = document.getElementById("uploadButtonMetadata");
    uploadButton.style.display = "none";
    
    let loadingButton = document.getElementById("loadingButton");
    loadingButton.style.display = "inline-block";
    
    let alertUpdate = document.getElementById("alert-success");
    
    fetch("/processMetaData", {
        method: "POST"
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message);
        
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
                if (value) {
                    translation = translation.replace("{0}", value);
                }
                element.textContent = translation;
                
                if (translation) {
                    if (element.tagName === "INPUT") {
                        const type = element.getAttribute("type")?.toLowerCase();
                        if (type === "submit") {
                            element.value = translation;
                        } else if (type === "button") {
                            element.value = translation;
                        } else {
                            element.placeholder = translation;
                        }
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
        
        directoryDisplay.innerHTML = `${folderPath}`;
        directoryDisplay.style.display = 'block';
    }
}

function openFolderSelector() {
    document.getElementById('folderInput').click();
}

document.addEventListener("DOMContentLoaded", function () {

    const folderPathTextarea = document.getElementById('folderPath');
    const button = document.getElementById('chooseFolder');

    window.api.readStoredPath().then(storedPath => {
        
      folderPathTextarea.value = storedPath;
    });

    button.addEventListener('click', async () => {
      const folderPath = await window.api.selectFolder();
      if (folderPath) {
        folderPathTextarea.value = folderPath;
      }
    });
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
    
    // Add event listener for folder input
    const folderInput = document.getElementById('folderInput');
    if (folderInput) {
        folderInput.addEventListener('change', selectFolder);
    }
});