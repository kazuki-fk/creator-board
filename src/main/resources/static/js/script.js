console.log("CreatorBoard script loaded.");

function showFileName(input) {
    const fileName = input.files[0]
        ? input.files[0].name
        : 'ファイルが選択されていません';
    document.getElementById('fileName').textContent = fileName;
}

