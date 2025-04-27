window.addEventListener('load', function () {
    const errorParam = new URLSearchParams(window.location.search).get('error');
    if (errorParam === 'needLogin') {
        alert('로그인이 필요한 기능입니다.');
    }
});

document.getElementById('login').addEventListener('submit', async function (e) {
    e.preventDefault();

    const email = document.querySelector('#email').value;
    const password = document.querySelector('#password').value;

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password }),
            credentials: 'include' // ✅ 쿠키 포함 설정 중요
        });

        if (!response.ok) {
            throw new Error('로그인 실패');
        }

        alert("로그인 완료");
        window.location.href = '/';
    } catch (err) {
        alert('로그인에 실패했습니다. 다시 시도하세요.');
        console.error(err);
    }
});