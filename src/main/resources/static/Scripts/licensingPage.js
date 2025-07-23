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
                    } else if (element.tagName === "LABEL"){
                        element.textContent = translation;
                    }  else if (element.tagName === "TEXTAREA"){
                        element.placeholder = translation;
                    } else if (element.tagName === "SMALL"){
                        element.textContent = translation;
                    } else if (element.tagName === "INPUT"){
                        element.value = translation;
                    }else {
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
});
