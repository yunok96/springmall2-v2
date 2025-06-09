/**
 * @jest-environment jsdom
 * @typedef {import('jest').Mock} Mock
 */

/** @type {jest.Mock} */
global.fetch = jest.fn();

import { errorHandler, loginUser } from '../../user/login.js';

// DOM 초기화 & alert, window.location mocking
beforeEach(() => {
    document.body.innerHTML = `
        <form id="login">
            <input id="email" value="test@example.com" />
            <input id="password" value="password123" />
            <button type="submit">로그인</button>
        </form>
    `;

    // alert, confirm, fetch, location.href 모킹
    global.alert = jest.fn();
    global.confirm = jest.fn();
    global.fetch = jest.fn();
});

afterEach(() => {
    jest.resetAllMocks();
});

describe('errorHandler', () => {

    test('alerts 로그인 필요 메시지 when error=needLogin', () => {
        // given
        window.history.pushState({}, 'Test page', '/?error=needLogin');

        // when
        errorHandler("needLogin");

        // then
        expect(alert).toHaveBeenCalledWith('로그인이 필요한 기능입니다.');
    });

    test('alert 표시 안함 when error!=needLogin', () => {
        // given
        window.history.pushState({}, 'Test page', '/');

        // when
        errorHandler();

        // then
        expect(window.alert).not.toHaveBeenCalled();
    });
});

describe('loginUser', () => {

    test('로그인 성공', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: true,
            })
        );

        // when
        await expect(loginUser('test@example.com', 'password')).resolves.toBe(true);

        // then
        expect(fetch).toHaveBeenCalledWith('/api/login', expect.objectContaining({
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'test@example.com', password: 'password' }),
            credentials: 'include',
        }));
    });

    test('로그인 실패', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: false,
            })
        );

        // when
        const promise = loginUser('test@example.com', 'password');

       // then
        await expect(promise).rejects.toThrow('로그인 실패');
    });
});
