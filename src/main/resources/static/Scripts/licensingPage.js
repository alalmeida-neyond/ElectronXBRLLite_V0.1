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
                const translation = data[key];
                console.log(element.tagName);
                console.log(translation);
                if (translation) {
                    if (element.tagName === "INPUT") {
                        const type = element.getAttribute("type")?.toLowerCase();
                        if (type === "submit") {
                            console.log("Yes");
                            element.value = translation;
                        } else if (type === "button") {
                            element.value = translation;
                            console.log("Also Yes");
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

function selectionForms ()
{
    const optionRegistration = document.getElementById("registration");
    const optionValidation = document.getElementById("validation");

    if (optionRegistration.checked) {
        document.getElementById("licensing").style.display = "none";
        document.getElementById("requestLicense").style.display = "block";
    } else if (optionValidation.checked){
        document.getElementById("licensing").style.display = "block";
        document.getElementById("requestLicense").style.display = "none";
    }
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
