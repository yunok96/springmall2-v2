/**
 * @jest-environment jsdom
 * @typedef {import('jest').Mock} Mock
 */

/** @type {jest.Mock} */
global.fetch = jest.fn();

import { initializeResetPassword } from '../../user/forgotPassword';  // 파일 경로 맞게 조정

beforeEach(() => {
    // 기본적인 DOM 셋업
    document.body.innerHTML = `
      <input type="text" id="email" />
      <button id="reset-password"></button>
    `;

    // alert, confirm, fetch, location.href 모킹
    global.alert = jest.fn();
    global.confirm = jest.fn();
    global.fetch = jest.fn();
});

afterEach(() => {
    jest.resetAllMocks();
});

describe('initializeResetPassword', () => {

    test('이메일이 공백이면 alert 호출 후 함수 종료', async () => {
        document.querySelector('#email').value = '  ';
        await initializeResetPassword();
        expect(alert).toHaveBeenCalledWith('이메일을 입력하세요.');
        expect(fetch).not.toHaveBeenCalled();
    });

    test('confirm이 false면 함수 종료', async () => {
        document.querySelector('#email').value = 'test@example.com';
        confirm.mockReturnValue(false);
        await initializeResetPassword();
        expect(fetch).not.toHaveBeenCalled();
    });

    test('가입한 이메일이 존재하지 않으면 alert 호출', async () => {
        document.querySelector('#email').value = 'test@example.com';
        confirm.mockReturnValue(true);
        fetch.mockResolvedValueOnce({ ok: false });  // 첫번째 fetch 실패
        await initializeResetPassword();
        expect(alert).toHaveBeenCalledWith('가입한 이메일이 존재하지 않습니다.');
    });

    test('비밀번호 초기화 성공 시 alert, location 변경', async () => {
        document.querySelector('#email').value = 'test@example.com';
        confirm.mockReturnValue(true);
        fetch.mockResolvedValueOnce({ ok: true });  // 이메일 체크 성공
        fetch.mockResolvedValueOnce({ ok: true });  // 초기화 성공
        await initializeResetPassword();
        expect(alert).toHaveBeenCalledWith('비밀번호가 초기화되었습니다. 이메일을 확인하세요.');
    });

    test('비밀번호 초기화 실패 시 alert 호출', async () => {
        document.querySelector('#email').value = 'test@example.com';
        confirm.mockReturnValue(true);
        fetch.mockResolvedValueOnce({ ok: true });  // 이메일 체크 성공
        fetch.mockResolvedValueOnce({ ok: false });  // 초기화 실패
        await initializeResetPassword();
        expect(alert).toHaveBeenCalledWith('비밀번호 초기화에 실패했습니다.');
    });

    test('fetch 예외 발생 시 alert 호출', async () => {
        document.querySelector('#email').value = 'test@example.com';
        confirm.mockReturnValue(true);
        fetch.mockRejectedValue(new Error('Network error'));
        await initializeResetPassword();
        expect(alert).toHaveBeenCalledWith('Network error');
    });
});
