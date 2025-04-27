async function getURL(e){
    let file = e.files[0]
    let name = encodeURIComponent(file.name)
    let result = await fetch('/presignedUrlTest?filename=' + name)
    result = await result.text()

    let 결과 = await fetch(result, {
        method: 'PUT',
        body: e.files[0]
    })

    console.log(결과.url.split("?")[0])
    if (결과.ok) {
        document.querySelector('img').src = 결과.url.split("?")[0]
    }
}