document.getElementById('productForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const form = this;
    const productId = form.productId.value;
    const quantity = form.quantity.value;

    const clickedButton = event.submitter; // 누른 버튼을 잡아냄
    const buttonId = clickedButton.id;

    if (buttonId === 'productOrder') {
        // 주문 처리 (예: GET 이동)
        window.location.href = `/productOrder?productId=${productId}&quantity=${quantity}`;
    }
    // TODO : 장바구니 기능 추후 추가
    else if (buttonId === 'addToCart') {
        // 장바구니 처리 (POST fetch)
        fetch('/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ productId, quantity }),
            credentials: 'include'
        })
            .then(res => {
                if (!res.ok) throw new Error('장바구니 추가 실패');
                return res.json();
            })
            .then(data => alert('장바구니에 담겼습니다.'))
            .catch(err => {
                console.error(err);
                alert('오류가 발생했습니다.');
            });
    }

});