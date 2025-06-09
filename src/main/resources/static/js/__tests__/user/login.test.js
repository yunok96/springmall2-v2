/**
 * @jest-environment jsdom
 */

import fs from 'fs';
import path from 'path';

// fetch mocking을 위해 설치 필요: npm install --save-dev whatwg-fetch
import 'whatwg-fetch';

describe('login.js', () => {
    let container;

    beforeEach(() => {
        // HTML 셋업
        document.body.innerHTML = `
      <form id="login">
        <input type="email" id="email" value="test@example.com" />
        <input type="password" id="password" value="password123" />
        <button type="submit">Login</button>
      </form>
    `;

        // JS 코드 실행
        const scriptContent = fs.readFileSync(path.resolve(__dirname, '../login.js'), 'utf8');
        eval(scriptContent);

        // fetch 모킹 초기화
        global.fetch = jest.fn();
        jest.clearAllMocks();
    });

    it('should alert when URL has error=needLogin', () => {
        const alertMock = jest.spyOn(window, 'alert').mockImplementation(() => {});
        delete window.location;
        window.location = {
            search: '?error=needLogin'
        };

        // 트리거를 위해 이벤트 수동 실행
        window.dispatchEvent(new Event('load'));

        expect(alertMock).toHaveBeenCalledWith('로그인이 필요한 기능입니다.');
        alertMock.mockRestore();
    });

    it('should call fetch with correct data on form submit', async () => {
        const mockResponse = {
            ok: true,
            json: async () => ({ token: 'fake-jwt' })
        };
        global.fetch.mockResolvedValue(mockResponse);

        const alertMock = jest.spyOn(window, 'alert').mockImplementation(() => {});
        delete window.location;
        window.location = { href: '' };

        const form = document.getElementById('login');
        form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

        // 비동기 반영
        await new Promise(setImmediate);

        expect(fetch).toHaveBeenCalledWith('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'test@example.com', password: 'password123' }),
            credentials: 'include'
        });

        expect(alertMock).toHaveBeenCalledWith('로그인 완료');
        expect(window.location.href).toBe('/');
        alertMock.mockRestore();
    });

    it('should alert on login failure', async () => {
        global.fetch.mockResolvedValue({ ok: false });

        const alertMock = jest.spyOn(window, 'alert').mockImplementation(() => {});
        const errorMock = jest.spyOn(console, 'error').mockImplementation(() => {});

        const form = document.getElementById('login');
        form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

        await new Promise(setImmediate);

        expect(alertMock).toHaveBeenCalledWith('로그인에 실패했습니다. 다시 시도하세요.');
        expect(console.error).toHaveBeenCalled();

        alertMock.mockRestore();
        errorMock.mockRestore();
    });
});
