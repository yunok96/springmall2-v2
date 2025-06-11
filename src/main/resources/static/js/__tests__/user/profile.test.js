/**
 * @jest-environment jsdom
 * @typedef {import('jest').Mock} Mock
 */

import * as module from '../../user/profile.js';

beforeEach(() => {
    global.alert = jest.fn();
    global.confirm = jest.fn();
    global.fetch = jest.fn();
});

afterEach(() => {
    jest.resetAllMocks();
});

describe('setupPasswordResetButton', () => {

    beforeEach(() => {
        document.body.innerHTML = `<button id="resetPasswordBtn">비밀번호 초기화</button>`;
    })

    test('예외 발생 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        module.setupPasswordResetButton();

        global.fetch = jest.fn(() =>
            Promise.reject(new Error('Fetch failed'))
        );

        // when
        document.getElementById('resetPasswordBtn').click();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호 재설정 메일 발송 중 오류가 발생했습니다.");
    });

    test('비밀번호 초기화 실패 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        module.setupPasswordResetButton();

        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: false,
            })
        );

        // when
        document.getElementById('resetPasswordBtn').click();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호 초기화에 실패했습니다.");
    });

    test('비밀번호 초기화 성공 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        module.setupPasswordResetButton();

        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: true,
            })
        );

        // when
        document.getElementById('resetPasswordBtn').click();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호가 초기화되었습니다. 이메일을 확인하세요.");
    });
});


describe('loadAddressList', () => {

    test('주소 목록 없는 경우', async () => {
        // given
        document.body.innerHTML = `
            <div id="noAddressAlert" class="d-none"></div>
            <div id="addressList" class="row row-cols-1 row-cols-md-2 g-4 d-none"></div>
        `;

        global.fetch = jest.fn().mockResolvedValue({
            json: () => Promise.resolve(null),
        });

        // when
        await module.loadAddressList();

        // then
        const alert = document.getElementById("noAddressAlert");
        const list = document.getElementById("addressList");
        expect(alert.classList.contains("d-none")).toBe(false);
        expect(list.classList.contains("d-none")).toBe(true);
    });

    test('주소 목록 있는 경우', async () => {
        // given
        document.body.innerHTML = `
            <div id="noAddressAlert" class="d-none"></div>
            <div id="addressList" class="row row-cols-1 row-cols-md-2 g-4 d-none"></div>
        `;

        const dummyAddress = [{
            id: 1,
            recipientName: "홍길동",
            zipCode: "12345",
            addressLine1: "서울시 강남구",
            addressLine2: "101호",
            phoneNumber: "010-1234-5678",
            default: true,
            createAt: "2024-06-11T10:20:00",
        }];

        global.fetch = jest.fn().mockResolvedValue({
            json: () => Promise.resolve(dummyAddress),
        });

        // when
        await module.loadAddressList();

        // then
        const alert = document.getElementById("noAddressAlert");
        const list = document.getElementById("addressList");
        expect(alert.classList.contains("d-none")).toBe(true);
        expect(list.classList.contains("d-none")).toBe(false);

        const card = list.querySelector(".card");
        expect(card).not.toBeNull();
        expect(card.dataset.id).toBe("1");
        expect(card.querySelector(".card-title").textContent).toContain("홍길동");
        expect(card.querySelector(".badge")).not.toBeNull();

        expect(list.querySelector(".btn-primary")).not.toBeNull(); // 수정 버튼
        expect(list.querySelector(".btn-danger")).not.toBeNull();  // 삭제 버튼
    });

});
