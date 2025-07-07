function downloadMetadata(event) {
            event.preventDefault();

            var formData = new FormData(document.getElementById("metadataForm"));

            let uploadButton = document.getElementById("uploadButtonMetadata");
            uploadButton.disabled = true;
            uploadButton.value = "A Atualizar...";

            fetch("/processMetaData", {
                method: "POST"
            }).then(response => response.json())
                .then(data => {
                    alert(data.message); // Show API response

                    if (data.enabled) {
                        uploadButton.disabled = false;
                        uploadButton.value = "Atualizar Metadados";
                    }})
                    .catch(error => {
                        console.error("Error:", error);
                    })
                    .finally(() => {
                        alert('Para concluir a atualizacao dos metadados, por favor, feche a aplicacao e volte a abrir!')
                        uploadButton.disabled = false;
                        uploadButton.value = "Atualizar Metadados";
                    });

        
        }