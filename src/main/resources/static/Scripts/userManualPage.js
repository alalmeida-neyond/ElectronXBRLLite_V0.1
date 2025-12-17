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
                    element.innerHTML = translation;
                }
            });
        })
        .catch(error => {
            console.error("Erro ao carregar idioma:", error);
        });
}6

document.addEventListener("DOMContentLoaded", function () {
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

});
