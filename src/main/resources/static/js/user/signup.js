// 비밀번호 확인 일치 여부
function validatePassword() {
    // 비밀번호와 비밀번호 확인 값 가져오기
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorMessage = document.getElementById('passwordError');

    // 비밀번호가 일치하는지 확인
    if (password !== confirmPassword) {
        // 오류 메시지 표시
        errorMessage.style.display = 'block';
        return false;  // 폼 제출 방지
    } else {
        // 오류 메시지 숨기기
        errorMessage.style.display = 'none';
        return true;  // 폼 제출 허용
    }
}

// 중복 이메일 체크
async function duplicateEmailCheck(email) {
    const response = await fetch('/api/check-email-duplication', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email })  // email만 전달
    });

    if (response.status === 409) {
        const errJson = await response.json();
        throw new Error(errJson.message); // 여기서 서버 메시지를 그대로 전달
    }

    if (!response.ok) {
        throw new Error('서버 오류 발생');
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('signup');
    const errorBox = document.getElementById('duplicatedEmailError');

    form.addEventListener('submit', async function (e) {
        e.preventDefault(); // 기본 폼 제출 막기

        if (!validatePassword()) return; // 비밀번호 확인

        const formData = new FormData(form);
        const data = Object.fromEntries(formData);

        try {
            await duplicateEmailCheck(data.email); // 중복 사용자 확인

            // 회원가입 요청
            const res = await fetch('/api/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (res.ok) {
                alert('회원가입이 완료되었습니다.');
                window.location.href = '/login';
            }
        } catch (err) {
            errorBox.textContent = err.message;
            errorBox.style.display = 'block';
            console.error(err);
        }
    });
});