document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("#password-reset");

    form.addEventListener("submit", async (event) => {
        event.preventDefault(); // 기본 폼 제출 방지

        const token = form.querySelector('input[name="token"]').value;
        const newPassword = form.querySelector('input[name="newPassword"]').value;
        const confirmNewPassword = form.querySelector('input[name="confirmNewPassword"]').value;

        if (newPassword !== confirmNewPassword) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        try {
            const response = await fetch("/reset-password-post", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    token: token,
                    newPassword: newPassword
                })
            });

            const result = await response.json();

            if (response.ok) {
                alert(result.message);
                window.location.href = "/login"; // 로그인 페이지로 이동
            } else {
                alert(result.message); // 오류 메시지 표시
            }
        } catch (error) {
            alert("비밀번호 수정 처리 중 문제가 발생했습니다.");
            console.error("Error:", error);
        }
    });
});
