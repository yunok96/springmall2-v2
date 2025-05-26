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

});

function openPostcode() {
    document.getElementById('postcodeModal').style.display = 'block';

    new daum.Postcode({
        oncomplete: function(data) {
            // 선택된 주소 데이터를 폼에 채우기
            document.getElementById('zipCode').value = data.zonecode;
            document.getElementById('addressLine1').value = data.roadAddress || data.jibunAddress;

            // 모달 닫기
            document.getElementById('postcodeModal').style.display = 'none';
        },
        onclose: function(state) {
            document.getElementById('postcodeModal').style.display = 'none';
        }
    }).embed(document.getElementById('postcodeLayer'));
}

function closePostcodeModal() {
    document.getElementById('postcodeModal').style.display = 'none';
    document.getElementById('postcodeLayer').innerHTML = ''; // 주소창 초기화
}
