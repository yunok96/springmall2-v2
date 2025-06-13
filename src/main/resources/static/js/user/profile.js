export {
    setupPasswordResetButton
    , setupPostAddressButton
    , setupPutAddressButton
    , loadAddressList
    , createEditButton
    , createDeleteButton
    , deleteAddress
    , setupFindAddressBtn
};


document.addEventListener("DOMContentLoaded", async function () {
    setupPasswordResetButton();
    setupPostAddressButton();
    setupPutAddressButton();
    await loadAddressList();

    setupFindAddressBtn();
});

// 주소 등록 모달 내부에서 "주소 찾기" 버튼 클릭 이벤트
// 테스트 제외. 카카오 API
function setupFindAddressBtn() {
    document.getElementById('findAddressBtn').addEventListener('click', () => {
        new daum.Postcode({
            oncomplete: function(data) {
                document.getElementById("zipCode").value = data.zonecode;
                document.getElementById("addressLine1").value = data.roadAddress || data.jibunAddress;
                document.getElementById("addressLine2").focus();
            }
        }).open();
    });
}

// 비밀번호 초기화 버튼 세팅
function setupPasswordResetButton() {
    document.getElementById("resetPasswordBtn").addEventListener("click", async function () {
        if (confirm("비밀번호를 초기화하시겠습니까?\n초기화된 비밀번호가 이메일로 전송됩니다.")) {
            try {
                const res = await fetch("/request-password-reset", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "X-Requested-With": "XMLHttpRequest"
                    }
                });
                if (res.ok) {
                    alert("비밀번호가 초기화되었습니다. 이메일을 확인하세요.");
                } else {
                    alert("비밀번호 초기화에 실패했습니다.");
                }
            } catch (err) {
                console.error(err);
                alert("비밀번호 재설정 메일 발송 중 오류가 발생했습니다.");
            }
        }
    });
}

// 주소 등록 이벤트
function setupPostAddressButton() {
    document.getElementById("postAddress").addEventListener("submit", async function (e) {
        e.preventDefault();
        const form = document.getElementById("postAddress");

        const data = {
            recipientName: form.querySelector('input[name="recipientName"]').value,
            zipCode: form.querySelector('input[name="zipCode"]').value,
            addressLine1: form.querySelector('input[name="addressLine1"]').value,
            addressLine2: form.querySelector('input[name="addressLine2"]').value,
            phoneNumber: form.querySelector('input[name="phoneNumber"]').value,
            default: form.querySelector('input[name="default"]').checked
        };

        try {
            const response = await fetch("/api/address/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });
            const result = await response.json();

            if (response.ok) {
                alert(result.message);

                // 모달 닫기
                const modal = bootstrap.Modal.getInstance(document.getElementById("addressRegisterModal"));
                modal.hide();

                // 폼 초기화
                document.getElementById("postAddress").querySelectorAll('input').forEach(input => {
                    if (input.type === 'checkbox' || input.type === 'radio') input.checked = false;
                    else input.value = '';
                });
                // document.getElementById("postAddress").reset();

                await loadAddressList(); // 주소 리스트 새로고침
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("에러 발생:", error);
            alert("오류가 발생했습니다.");
        }
    });
}

// 주소 수정 이벤트
function setupPutAddressButton() {
    document.getElementById("putAddress").addEventListener("submit", async function (e) {
        e.preventDefault();

        const form = document.getElementById("putAddress");

        const data = {
            id: form.querySelector('input[name="id"]').value,
            recipientName: form.querySelector('input[name="recipientName"]').value,
            zipCode: form.querySelector('input[name="zipCode"]').value,
            addressLine1: form.querySelector('input[name="addressLine1"]').value,
            addressLine2: form.querySelector('input[name="addressLine2"]').value,
            phoneNumber: form.querySelector('input[name="phoneNumber"]').value,
            default: form.querySelector('input[name="default"]').checked
        };

        try {
            const response = await fetch("/api/address/update", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });
            const result = await response.json();

            if (response.ok) {
                alert(result.message);

                // 모달 닫기
                const modal = bootstrap.Modal.getInstance(document.getElementById("addressUpdateModal")); // modal1 → 모달의 id
                modal.hide();

                // register form initialize
                document.getElementById("putAddress").reset();

                // refresh address list
                await loadAddressList();

            } else {
                alert(result.message);
            }

        } catch (error) {
            console.error("에러 발생:", error);
            alert("오류가 발생했습니다.");
        }
    });
}

// 주소 목록 호출 API
async function loadAddressList() {
    const response = await fetch("/api/address/list");
    const addresses = await response.json();

    const noAddressAlert = document.getElementById("noAddressAlert");
    const addressList = document.getElementById("addressList");

    const addressListContainer = addressList; // div with class row row-cols-1 row-cols-md-2 g-4 d-none
    addressListContainer.innerHTML = ""; // 기존 내용 초기화

    if (!addresses || addresses.length === 0) {
        // 주소 없을 때
        noAddressAlert.classList.remove("d-none");
        addressList.classList.add("d-none");
    } else {
        // 주소 있을 때
        noAddressAlert.classList.add("d-none");
        addressList.classList.remove("d-none");

        addresses.forEach(address => {
            // 각 주소마다 카드 div.col 생성
            const col = document.createElement("div");
            col.className = "col";

            const card = document.createElement("div");
            card.className = "card h-100";
            card.dataset.id = address.id;

            const cardBody = document.createElement("div");
            cardBody.className = "card-body";

            // 카드 타이틀 (수령인 + 기본배송지 배지)
            const title = document.createElement("h5");
            title.className = "card-title";
            title.textContent = address.recipientName;

            if (address.default) {
                const badge = document.createElement("span");
                badge.className = "badge bg-primary ms-2";
                badge.textContent = "기본배송지";
                title.appendChild(badge);
            }
            cardBody.appendChild(title);

            // 우편번호
            const zip = document.createElement("p");
            zip.className = "card-text mb-1";
            zip.innerHTML = `<strong>우편번호:</strong> ${address.zipCode}`;
            cardBody.appendChild(zip);

            // 주소
            const addr1 = document.createElement("p");
            addr1.className = "card-text mb-1";
            addr1.innerHTML = `<strong>주소:</strong> ${address.addressLine1}`;
            cardBody.appendChild(addr1);

            // 상세주소
            const addr2 = document.createElement("p");
            addr2.className = "card-text mb-1";
            addr2.innerHTML = `<strong>상세주소:</strong> ${address.addressLine2}`;
            cardBody.appendChild(addr2);

            // 전화번호
            const phone = document.createElement("p");
            phone.className = "card-text mb-1";
            phone.innerHTML = `<strong>전화번호:</strong> ${address.phoneNumber}`;
            cardBody.appendChild(phone);

            // 등록일 (yyyy-MM-dd HH:mm 형식으로 가정)
            const regDate = document.createElement("p");
            regDate.className = "card-text";
            const formattedDate = new Date(address.createAt).toLocaleString("ko-KR", {
                year: "numeric",
                month: "2-digit",
                day: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
            });
            regDate.innerHTML = `<strong>등록일:</strong> ${formattedDate}`;
            cardBody.appendChild(regDate);
            card.appendChild(cardBody);

            // 카드 푸터 - 수정, 삭제 버튼
            const cardFooter = document.createElement("div");
            cardFooter.className = "card-footer text-end";

            const btnEdit = createEditButton(address);
            const btnDelete = createDeleteButton(address.id);

            cardFooter.appendChild(btnEdit);
            cardFooter.appendChild(btnDelete);

            card.appendChild(cardFooter);
            col.appendChild(card);

            addressListContainer.appendChild(col);
        });
    }
}

// 수정 버튼 생성 함수
function createEditButton(address) {
    const btnEdit = document.createElement("button");
    btnEdit.className = "btn btn-sm btn-primary me-2 text-white";
    btnEdit.textContent = "수정";

    const form = document.getElementById("putAddress");

    btnEdit.addEventListener("click", () => {
        // 모달 input 요소에 값 설정
        form.querySelector('input[name="id"]').value = address.id;
        form.querySelector('input[name="recipientName"]').value = address.recipientName;
        form.querySelector('input[name="zipCode"]').value = address.zipCode;
        form.querySelector('input[name="addressLine1"]').value = address.addressLine1;
        form.querySelector('input[name="addressLine2"]').value = address.addressLine2;
        form.querySelector('input[name="phoneNumber"]').value = address.phoneNumber;
        form.querySelector('input[name="default"]').checked = address.default;

        // 모달 열기
        const modal = new bootstrap.Modal(document.getElementById("addressUpdateModal"));
        modal.show();
    });

    return btnEdit;
}

// 삭제 버튼 생성 함수
function createDeleteButton(addressId) {
    const btnDelete = document.createElement("button");
    btnDelete.className = "btn btn-sm btn-danger text-white";
    btnDelete.textContent = "삭제";

    btnDelete.addEventListener("click", () => {
        if (confirm("배송지를 삭제하시겠습니까?")) {
            deleteAddress(addressId);
        }
    });

    return btnDelete;
}

// 주소 삭제 api
async function deleteAddress(addressId) {
    const data = {
        id: addressId
    };

    try {
        const response = await fetch("/api/address/delete", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (response.ok) {
            alert(result.message);

            // refresh address list
            await loadAddressList();
        } else {
            alert(result.message);
        }

    } catch (error) {
        console.error("에러 발생:", error);
        alert("오류가 발생했습니다.");
    }
}