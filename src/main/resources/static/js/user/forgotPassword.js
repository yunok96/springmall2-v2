export { initializeResetPassword };

document.addEventListener("DOMContentLoaded", async function () {
    document.getElementById("reset-password").addEventListener("click", function () {
        initializeResetPassword();
    });
});

async function initializeResetPassword() {
    const email = document.querySelector("#email").value;
    if (!email.trim()) {
        alert("이메일을 입력하세요.");
        return;
    }

    if (!confirm("비밀번호를 초기화하시겠습니까?\n초기화된 비밀번호가 이메일로 전송됩니다.")) {
        return;
    }

    try {
        // 이메일 유효성 검사
        const checkRes = await fetch("/api/check-email-exists", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: JSON.stringify({ email })
        });

        if (!checkRes.ok) {
            throw new Error("가입한 이메일이 존재하지 않습니다.");
        }

        // 비밀번호 초기화 요청
        const resetRes = await fetch("/request-password-reset-by-email", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: JSON.stringify({ email })
        });

        if (resetRes.ok) {
            alert("비밀번호가 초기화되었습니다. 이메일을 확인하세요.");
            window.location.href = "/"
        } else {
            alert("비밀번호 초기화에 실패했습니다.");
        }

    } catch (error) {
        console.error(error);
        alert(error.message || "비밀번호 재설정 중 오류가 발생했습니다.");
    }
}