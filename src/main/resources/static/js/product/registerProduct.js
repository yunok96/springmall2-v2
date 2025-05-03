// 이미지 등록시 PreSignedURL 요청
async function getPreSignedUrl(fileName) {
    const response = await fetch('/getPreSignedUrl', {
        method: 'POST', // POST 메서드 사용
        headers: {
            'Content-Type': 'application/json', // 요청 본문이 JSON 형식임을 명시
        },
        body: JSON.stringify({ fileName: fileName }), // 요청 본문에 JSON 데이터 담아서 전송
    });

    if (!response.ok) {
        const errorMessage = await response.text(); // 또는 response.json() 시도 후 catch
        throw new Error(`PreSigned URL 요청 실패: ${response.status} - ${errorMessage}`);
    }

    const result = await response.json(); // JSON 형태로 파싱
    return result.url;
}

// S3에 파일 업로드
async function uploadFileToS3(preSignedUrl, file) {
    let result = await fetch(preSignedUrl, {
        method: 'PUT',
        body: file
    });

    if (!result.ok) {
        const errorMessage = await result.text();
        throw new Error(`파일 업로드 실패: ${result.status} - ${errorMessage}`);
    }
    return result.url.split("?")[0];
}

// 썸네일 이미지 업로드시 동작
async function uploadThumbnailImage(input) {
    try {
        if (input.files && input.files[0]) {
            const file = input.files[0];
            const preSignedUrl = await getPreSignedUrl(file.name);
            const imageUrl = await uploadFileToS3(preSignedUrl, file);

            // 썸네일 미리보기 표시
            if (imageUrl) {
                const fileKey = imageUrl.split("/").pop();

                document.getElementById('thumbnailPreview').src = imageUrl; // 미리보기 이미지 URL 설정. 내 서버 코드를 타지 않는 로직이기에 즉시 url 반영함.
                document.getElementById('thumbnailPreview').style.display = 'block';
                document.getElementById('thumbnailImageName').value = file.name;
                document.getElementById('thumbnailImageKey').value = fileKey;
            }
        }
    } catch (error) {
        console.error("파일 업로드 중 오류 발생:", error);
        alert("파일 업로드 중 오류가 발생했습니다");
    }
}

// 내용 이미지 업로드시 동작
async function uploadContentImages(input) {
    try {
        if (input.files && input.files[0]) {
            const file = input.files[0];
            const index = input.dataset.index;
            const preSignedUrl = await getPreSignedUrl(file.name);
            const imageUrl = await uploadFileToS3(preSignedUrl, file);

            // 썸네일 미리보기 표시
            if (imageUrl) {
                const fileKey = imageUrl.split("/").pop();

                const imageKeyInput = document.getElementById(`contentImageKey-${index}`);
                const imageNameInput = document.getElementById(`contentImageName-${index}`);
                const previewElement = document.getElementById(`contentImagePreview-${index}`);

                previewElement.src = imageUrl; // 미리보기 이미지 URL 설정. 내 서버 코드를 타지 않는 로직이기에 즉시 url 반영함.
                previewElement.style.display = 'block';
                imageKeyInput.value = fileKey;
                imageNameInput.value = file.name;

                // 새로운 상품 내용 이미지 input 추가
                const nextIndex = parseInt(index) + 1;
                addContentImageInput(nextIndex);
            }
        }
    } catch (error) {
        console.error("파일 업로드 중 오류 발생:", error);
        alert("파일 업로드 중 오류가 발생했습니다");
    }
}

// 내용 이미지 업로드 후 추가 input 생성 동작
function addContentImageInput(index) {
    const container = document.createElement('div');
    container.classList.add('mb-4', 'content-image-group');
    container.innerHTML = `
            <label for="contentImageFile-${index}" class="form-label">상품 내용 이미지 ${index}</label>
            <div class="row align-items-start">
                <div class="col-md-6">
                    <input class="form-control" type="file" id="contentImageFile-${index}" name="contentImageFiles"
                           accept="image/*" onchange="uploadContentImages(this)" data-index="${index}">
                </div>
                <div class="col-md-6 d-flex">
                    <img id="contentImagePreview-${index}" src="#" alt="내용 미리보기"
                         style="max-width: 200px; display: none; margin-right: 10px;">

                    <div class="d-flex flex-column">
                        <button type="button" class="btn btn-secondary btn-sm mb-1 up-btn"
                                onclick="moveContentImage(${index}, 'up')">↑</button>
                        <button type="button" class="btn btn-secondary btn-sm mb-1 down-btn"
                                onclick="moveContentImage(${index}, 'down')">↓</button>
                        <button type="button" class="btn btn-danger btn-sm remove-btn"
                                onclick="removeContentImage(${index})">X</button>
                    </div>
                </div>
            </div>
            <input type="hidden" id="contentImageKey-${index}" name="contentImageKeys">
            <input type="hidden" id="contentImageName-${index}" name="contentImageNames">
    `;
    const form = document.getElementById('registerProductForm');
    const submitButton = document.getElementById('submit-btn');
    form.insertBefore(container, submitButton);
}

// 썸네일 이미지 삭제 함수
function clearThumbnailImage() {
    // 이미지 input 초기화
    document.getElementById('thumbnailImageFile').value = '';

    // 미리보기 이미지 숨기기
    document.getElementById('thumbnailPreview').style.display = 'none';

    // hidden 필드 값 초기화
    document.getElementById('thumbnailImageKey').value = '';
    document.getElementById('thumbnailImageName').value = '';
}

// 내용 이미지 삭제
function removeContentImage(index) {
    // 버튼이 소속된 div 선택
    const container = document.querySelector(`#contentImageFile-${index}`)
        .closest('.content-image-group');
    if (!container) return;

    // 전체 내용 이미지 그룹
    const allGroups = Array.from(document.querySelectorAll('.content-image-group'));
    const currentIdx = allGroups.indexOf(container);

    // 마지막 요소인 경우, 작동 안함.
    if (currentIdx === allGroups.length - 1) return;

    // 첫번째 내용 이미지이고, 후속 내용 이미지가 없는 경우, 요소를 삭제하지 않고 썸네일과 파일 내용만 초기화
    if (index === 1 && allGroups < 2) {
        document.getElementById(`contentImageFile-${index}`).value = '';
        document.getElementById(`contentImagePreview-${index}`).style.display = 'none';
        document.getElementById(`contentImageKey-${index}`).value = '';
        document.getElementById(`contentImageName-${index}`).value = '';
    } else {
        container.remove(); // 해당 div 삭제
        resetContentImageIndices(); // 내용 이미지 인덱스 재설정
    }
}

// 내용 이미지 이동
function moveContentImage(index, direction) {
    // 버튼이 소속된 div 선택
    const container = document.querySelector(`#contentImageFile-${index}`)
        .closest('.content-image-group');
    if (!container) return;

    // 전체 내용 이미지 그룹
    const allGroups = Array.from(document.querySelectorAll('.content-image-group'));
    const currentIdx = allGroups.indexOf(container);

    let targetIdx = direction === 'up' ? currentIdx - 1 : currentIdx + 1;

    // 경계값 체크
    if (targetIdx < 0 || targetIdx >= allGroups.length - 1) return;

    const parent = container.parentNode;
    const target = allGroups[targetIdx];

    // 위치 스왑
    if (direction === 'up') {
        parent.insertBefore(container, target);
    } else {
        parent.insertBefore(target, container);
    }

    resetContentImageIndices(); // 내용 이미지 인덱스 재설정
}

// 내용 이미지 그룹의 인덱스 재설정
function resetContentImageIndices() {
    const allGroups = document.querySelectorAll('.content-image-group');
    allGroups.forEach((group, idx) => {
        const index = idx + 1;

        const label = group.querySelector('label');
        label.htmlFor = `contentImageFile-${index}`;
        label.textContent = `상품 내용 이미지 ${index}`;

        const file = group.querySelector('input[type="file"]');
        file.id = `contentImageFile-${index}`;
        file.setAttribute('data-index', `${index}`);

        const preview = group.querySelector('img');
        preview.id = `contentImagePreview-${index}`;

        const urlInput = group.querySelector('input[name="contentImageKeys"]');
        urlInput.id = `contentImageKey-${index}`;

        const nameInput = group.querySelector('input[name="contentImageNames"]');
        nameInput.id = `contentImageName-${index}`;

        // 버튼들에 새 인덱스를 반영
        const upBtn = group.querySelector('.up-btn');
        const downBtn = group.querySelector('.down-btn');
        const removeBtn = group.querySelector('.remove-btn');
        if (upBtn) upBtn.setAttribute('onclick', `moveContentImage(${index}, 'up')`);
        if (downBtn) downBtn.setAttribute('onclick', `moveContentImage(${index}, 'down')`);
        if (removeBtn) removeBtn.setAttribute('onclick', `removeContentImage(${index})`);
    });
}

// 상품 등록 폼 제출 이벤트 리스너
document.getElementById('registerProductForm').addEventListener('submit', async function(event) {
    event.preventDefault();
    let formData = new FormData(document.getElementById('registerProductForm'));
    // 파일 input 대신 S3 URL을 formData에 추가
    formData.delete('thumbnailImageFile');
    formData.delete('contentImageFiles');

    const contentImages = [];
    document.querySelectorAll('.content-image-group').forEach(group => {
        const url = group.querySelector('input[name="contentImageKeys"]').value;
        const name = group.querySelector('input[name="contentImageNames"]').value;
        if (url) {
            contentImages.push({
                fileName: name,
                fileKey: url
            });
        }
    });

    const productData = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        price: document.getElementById('price').value,
        stock: document.getElementById('stock').value,
        thumbnailImage: {
            fileName: document.getElementById('thumbnailImageName').value,
            fileKey: document.getElementById('thumbnailImageKey').value
        },
        contentImages
    };

    const response = await fetch('/registerProduct', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(productData)
    });

    if (response.ok) {
        alert('상품을 등록했습니다.');
        window.location.href = '/'; // TODO : 성공 시 목록 페이지로 이동
    } else {
        const result = await response.text();
        console.error('상품 등록 오류:', result);
        alert('상품 등록 중 오류가 발생했습니다.');
    }
});