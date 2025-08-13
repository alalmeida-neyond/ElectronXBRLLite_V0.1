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
        alert(data.message); // Show API response
        
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

// Function to handle folder selection and display directory
function selectFolder() {
    const folderInput = document.getElementById('folderInput');
    const directoryDisplay = document.getElementById('directoryDisplay');
    
    if (folderInput.files.length > 0) {
        // Get the first file to extract the folder path
        const firstFile = folderInput.files[0];
        const folderPath = firstFile.webkitRelativePath.split('/')[0];
        
        // Display the directory name
        directoryDisplay.innerHTML = `<strong>Selected Directory:</strong> ${folderPath}`;
        directoryDisplay.style.display = 'block';
        
        console.log('Selected folder:', folderPath);
        console.log('Number of files in folder:', folderInput.files.length);
    }
}

// Function to trigger folder selection
function openFolderSelector() {
    document.getElementById('folderInput').click();
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
    
    // Add event listener for folder input
    const folderInput = document.getElementById('folderInput');
    if (folderInput) {
        folderInput.addEventListener('change', selectFolder);
    }
});