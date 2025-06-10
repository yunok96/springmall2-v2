export { errorHandler, loginUser };

document.addEventListener('DOMContentLoaded', function () {
    errorHandler();
    setupLoginForm();
});

function setupLoginForm() {
    document.getElementById('login').addEventListener('submit', async function (e) {
        e.preventDefault();
        const email = document.querySelector('#email').value;
        const password = document.querySelector('#password').value;

        try {
            await loginUser(email, password);
            alert("로그인 완료");
            window.location.href = '/';
        } catch (err) {
            alert('로그인에 실패했습니다. 다시 시도하세요.');
            console.error(err);
        }
    });
}

function errorHandler() {
    const errorParam = new URLSearchParams(window.location.search).get('error');
    if (errorParam === 'needLogin') {
        alert('로그인이 필요한 기능입니다.');
    }
}

async function loginUser(email, password) {
    const response = await fetch('/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include' // 쿠키 포함
    });

    if (!response.ok) {
        throw new Error('로그인 실패');
    }

    return true; // 성공 시 true 반환
}