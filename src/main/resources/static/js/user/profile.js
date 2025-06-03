document.addEventListener("DOMContentLoaded", function () {

    // "비밀번호 초기화" 버튼 클릭 이벤트
    const resetPasswordBtn = document.getElementById("resetPasswordBtn");
    if (resetPasswordBtn) {
        resetPasswordBtn.addEventListener("click", function () {
            if (confirm("비밀번호를 초기화하시겠습니까?\n초기화된 비밀번호가 이메일로 전송됩니다.")) {
                fetch("/request-password-reset", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        "X-Requested-With": "XMLHttpRequest"
                    }
                }).then(res => {
                    if (res.ok) {
                        alert("비밀번호가 초기화되었습니다. 이메일을 확인하세요.");
                    } else {
                        alert("비밀번호 초기화에 실패했습니다.");
                    }
                }).catch(err => {
                    console.error(err);
                    alert("비밀번호 재설정 메일 발송 중 오류가 발생했습니다.");
                });
            }
        });
    }

    // 주소 등록 모달 내부에서 "주소 찾기" 버튼 클릭 이벤트
    document.getElementById('findAddressBtn').addEventListener('click', () => {
        new daum.Postcode({
            oncomplete: function(data) {
                document.getElementById("zipCode").value = data.zonecode;
                document.getElementById("addressLine1").value = data.roadAddress || data.jibunAddress;
                document.getElementById("addressLine2").focus();
            }
        }).open();
    });

    // 주소 등록 이벤트
    document.getElementById("postAddress").addEventListener("submit", async function (e) {
        e.preventDefault();

        const form = document.getElementById("postAddress");

        const data = {
            recipientName: form.querySelector('input[name="recipientName"]').value,
            zipCode: form.querySelector('input[name="zipCode"]').value,
            addressLine1: form.querySelector('input[name="addressLine1"]').value,
            addressLine2: form.querySelector('input[name="addressLine2"]').value,
            phoneNumber: form.querySelector('input[name="phoneNumber"]').value,
            default: form.querySelector('input[name="defaultAddress"]').checked
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
                const modal = bootstrap.Modal.getInstance(document.getElementById("addressRegisterModal")); // modal1 → 모달의 id
                modal.hide();

                // register form initialize
                document.getElementById("postAddress").reset();

                // 주소 목록 갱신



            } else {
                alert(result.message);
            }

        } catch (error) {
            console.error("에러 발생:", error);
            alert("오류가 발생했습니다.");
        }
    });

    // 주소 목록 호출 API
    async function loadAddressList() {
        const response = await fetch("/api/address/list");
        const addresses = await response.json();

        const listContainer = document.getElementById("addressList");
        listContainer.innerHTML = ""; // 기존 목록 비우기

        addresses.forEach(address => {
            const item = document.createElement("div");
            item.className = "address-item mb-2 p-2 border rounded";
            item.innerHTML = `
            <strong>${address.recipientName}</strong> (${address.phoneNumber})<br>
            ${address.addressLine1} ${address.addressLine2} (${address.zipCode})<br>
            ${address.default ? "<span class='badge bg-primary'>기본배송지</span>" : ""}
        `;
            listContainer.appendChild(item);
        });
    }

});