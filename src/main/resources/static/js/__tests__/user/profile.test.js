/**
 * @jest-environment jsdom
 * @typedef {import('jest').Mock} Mock
 */

import * as profileModule from '../../user/profile.js';

// DOM 초기화 & alert, window.location mocking
beforeEach(() => {
    // alert, confirm, fetch, location.href 모킹
    global.alert = jest.fn();
    global.confirm = jest.fn();
    global.fetch = jest.fn();

    // Setup for preventing DOM error
    document.body.innerHTML = `
        <button id="resetPasswordBtn"></button>
        <button id="findAddressBtn"></button>
        <input id="zipCode" type="text" />
        <input id="addressLine1" type="text" />
        <input id="addressLine2" type="text" />
    `;
});

afterEach(() => {
    jest.resetAllMocks();
});

describe('setupPasswordResetButton', () => {

    beforeEach(() => {
        document.body.innerHTML = `<button id="resetPasswordBtn">비밀번호 초기화</button>`;

        profileModule.sendPasswordResetRequest = jest.fn();
    })

    test('confirm 누르지 않을 경우', () => {
        // given
        global.confirm.mockReturnValue(false);
        profileModule.setupPasswordResetButton();

        // when
        document.getElementById('resetPasswordBtn').click();

        // then
        expect(profileModule.sendPasswordResetRequest).not.toHaveBeenCalled(); // 직접 모의한 함수 사용
        expect(alert).not.toHaveBeenCalled();
    });

    test('예외 발생 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        profileModule.sendPasswordResetRequest.mockRejectedValue(new Error("network error"));
        profileModule.setupPasswordResetButton();

        // when
        document.getElementById('resetPasswordBtn').click();

        await Promise.resolve();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호 재설정 메일 발송 중 오류가 발생했습니다.");
    });

    test('비밀번호 초기화 실패 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        profileModule.sendPasswordResetRequest.mockResolvedValue({ ok: false });  // 이렇게 기존 spy에 mock값 지정
        profileModule.setupPasswordResetButton();

        // when
        document.getElementById('resetPasswordBtn').click();

        await Promise.resolve();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호 초기화에 실패했습니다.");
    });

    test('비밀번호 초기화 성공 시', async () => {
        // given
        global.confirm.mockReturnValue(true);
        profileModule.sendPasswordResetRequest.mockResolvedValue({ ok: true });
        profileModule.setupPasswordResetButton();

        // when
        document.getElementById('resetPasswordBtn').click();

        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("비밀번호가 초기화되었습니다. 이메일을 확인하세요.");
    });
});
