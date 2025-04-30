// 이미지 등록시 PreSignedURL 요청
async function getPreSignedUrl(filename) {
    let result = await fetch('/getPreSignedUrl?filename=' + encodeURIComponent(filename));
    result = await result.json(); // JSON 형태로 파싱
    return result.url;
}

// S3에 파일 업로드
async function uploadFileToS3(preSignedUrl, file) {
    let result = await fetch(preSignedUrl, {
        method: 'PUT',
        body: file
    });
    if (result.ok) {
        return result.url.split("?")[0];
    } else {
        console.error("S3 업로드 실패:", result);
        return null;
    }
}

// 썸네일 이미지 변경시 동작
async function uploadThumbnailImage(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const preSignedUrl = await getPreSignedUrl(file.name);
        const imageUrl = await uploadFileToS3(preSignedUrl, file);

        // 썸네일 미리보기 표시
        if (imageUrl) {
            document.getElementById('thumbnailPreview').src = imageUrl;
            document.getElementById('thumbnailPreview').style.display = 'block';
            document.getElementById('thumbnailImageUrl').value = imageUrl;
        }
    }
}

// 내용 이미지 변경시 동작
async function uploadContentImages(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const preSignedUrl = await getPreSignedUrl(file.name);

        try {
            const imageUrl = await uploadFileToS3(preSignedUrl, file);
            console.log("imageURL : "+ imageUrl);

            const index = input.dataset.index; // data-index 속성 값 읽어오기
            const previewId = `contentImagePreview-${index}`;
            const imageUrlId = `contentImageUrl-${index}`;
            const imageUrlsInput = document.getElementById('contentImageUrls');
            let imageUrls = imageUrlsInput.value ? JSON.parse(imageUrlsInput.value) : [];

            if (imageUrl) {
                const previewElement = document.getElementById(previewId);
                if (previewElement) {
                    previewElement.src = imageUrl;
                    previewElement.style.display = 'block';
                }
                imageUrls.push(imageUrl);
                imageUrlsInput.value = JSON.stringify(imageUrls);

                // 새로운 상품 내용 이미지 input 추가
                const nextIndex = parseInt(index) + 1;
                addContentImageInput(nextIndex);
            }
        } catch (error) {
            alert("파일 업로드 중 오류가 발생했습니다.");
            console.error("파일 업로드 오류:", error);
        }
    }
}

function addContentImageInput(index) {
    const container = document.createElement('div');
    container.classList.add('mb-4');
    container.innerHTML = `
        <label for="contentImageFile-${index}" class="form-label">상품 내용 이미지 ${index}</label>
        <input class="form-control" type="file" id="contentImageFile-${index}" name="contentImageFiles" 
               accept="image/*" onchange="uploadContentImages(this)" data-index="${index}">
        <div id="contentImagePreview-${index}" style="display: none; flex-wrap: wrap; margin-top: 10px;"></div>
        <input type="hidden" id="contentImageUrl-${index}" name="contentImageUrls">
    `;
    document.getElementById('registerProductForm').insertBefore(container, document.querySelector('button[type="button"]'));
}

// 상품 등록 폼 제출
async function submitProduct() {
    const formData = new FormData(document.getElementById('registerProductForm'));
    // 파일 input 대신 S3 URL을 formData에 추가
    formData.delete('thumbnailImageFile');
    formData.delete('contentImageFiles');
    formData.append('thumbnailImageUrl', document.getElementById('thumbnailImageUrl').value);
    formData.append('contentImageUrls', document.getElementById('contentImageUrls').value);

    const response = await fetch('/registerProduct', {
        method: 'POST',
        body: new URLSearchParams([...formData]), // form-urlencoded 형태로 전송
    });

    if (response.ok) {
        window.location.href = '/productList'; // 성공 시 목록 페이지로 이동
    } else {
        const result = await response.text();
        alert('상품 등록 실패: ' + result);
    }
}