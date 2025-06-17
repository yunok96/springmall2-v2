/**
 * @jest-environment jsdom
 * @typedef {import('jest').Mock} Mock
 */

import * as module from '../../user/profile.js';
import {createDeleteButton, deleteAddress, setupPutAddressButton} from "../../user/profile.js";

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

describe('setupPostAddressButton', () => {

    let form;

    beforeEach(() => {
        document.body.innerHTML = `
          <form id="postAddress">
            <input name="recipientName" value="홍길동" />
            <input name="zipCode" value="12345" />
            <input name="addressLine1" value="서울시 강남구" />
            <input name="addressLine2" value="101호" />
            <input name="phoneNumber" value="010-1234-5678" />
            <input type="checkbox" name="default" checked />
            <button type="submit">제출</button>
          </form>
          <div id="addressRegisterModal" class="modal"></div>
          <div id="noAddressAlert" class="d-none"></div>
          <div id="addressList" class="row row-cols-1 row-cols-md-2 g-4 d-none"></div>
        `;

        // 모달 관련 메서드 모킹
        window.bootstrap = {
            Modal: {
                getInstance: () => ({
                    hide: jest.fn(),
                }),
            },
        };

        form = document.getElementById("postAddress");
    });


    test('fetch 예외 발생 시', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.reject(new Error('Fetch failed'))
        );

        const submitEvent = new Event("submit");

        module.setupPostAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("오류가 발생했습니다.");
    });

    test('fetch 결과가 Not Ok', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: false,
                json: () => Promise.resolve({ message: "주소 등록 실패" }),
            })
        );

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPostAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("주소 등록 실패");
    });

    test('fetch 결과가 Ok', async () => {
        // given
        global.fetch.mockImplementation((url) => {
            if (url.endsWith('/register')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ message: "주소 등록 성공" }),
                });
            }

            if (url.endsWith('/list')) {
                return Promise.resolve({
                    ok: true,
                    json: async () => (
                        [{
                            id: 1,
                        }]
                    ),
                });
            }
        });

        const hideMock = jest.fn();
        const getInstanceMock = jest.fn(() => ({
            hide: hideMock,
        }));
        window.bootstrap = {
            Modal: {
                getInstance: getInstanceMock,
            },
        };

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPostAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        // fetch 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/register", expect.objectContaining({
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: expect.any(String),
        }));

        // alert 호출 검증
        expect(alert).toHaveBeenCalledWith("주소 등록 성공");

        // 모달 숨김 검증
        expect(getInstanceMock).toHaveBeenCalledWith(document.getElementById("addressRegisterModal"));
        expect(hideMock).toHaveBeenCalled();

        // input 리셋 검증
        const input = document.querySelector('form#postAddress input[name="recipientName"]');
        expect(input.value).toBe('');

        // loadAddressList 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/list");
    });
});

describe('setupPutAddressButton', () => {

    let form;

    beforeEach(() => {
        document.body.innerHTML = `
          <form id="putAddress">
            <input name="id" value="1" />
            <input name="recipientName" value="홍길동" />
            <input name="zipCode" value="12345" />
            <input name="addressLine1" value="서울시 강남구" />
            <input name="addressLine2" value="101호" />
            <input name="phoneNumber" value="010-1234-5678" />
            <input type="checkbox" name="default" checked />
            <button type="submit">제출</button>
          </form>
          <div id="addressUpdateModal" class="modal"></div>
          <div id="noAddressAlert" class="d-none"></div>
          <div id="addressList" class="row row-cols-1 row-cols-md-2 g-4 d-none"></div>
        `;

        // 모달 관련 메서드 모킹
        window.bootstrap = {
            Modal: {
                getInstance: () => ({
                    hide: jest.fn(),
                }),
            },
        };

        form = document.getElementById("putAddress");
    });


    test('fetch 예외 발생 시', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.reject(new Error('Fetch failed'))
        );

        const submitEvent = new Event("submit");

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("오류가 발생했습니다.");
    });

    test('fetch 결과가 Not Ok', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: false,
                json: () => Promise.resolve({ message: "주소 수정 실패" }),
            })
        );

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("주소 수정 실패");
    });

    test('fetch 결과가 Ok', async () => {
        // given
        global.fetch.mockImplementation((url) => {
            if (url.endsWith('/update')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ message: "주소 수정 성공" }),
                });
            }

            if (url.endsWith('/list')) {
                return Promise.resolve({
                    ok: true,
                    json: async () => (
                        [{
                            id: 1,
                        }]
                    ),
                });
            }
        });

        const hideMock = jest.fn();
        const getInstanceMock = jest.fn(() => ({
            hide: hideMock,
        }));
        window.bootstrap = {
            Modal: {
                getInstance: getInstanceMock,
            },
        };

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        // fetch 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/update", expect.objectContaining({
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: expect.any(String),
        }));

        // alert 호출 검증
        expect(alert).toHaveBeenCalledWith("주소 수정 성공");

        // 모달 숨김 검증
        expect(getInstanceMock).toHaveBeenCalledWith(document.getElementById("addressUpdateModal"));
        expect(hideMock).toHaveBeenCalled();

        // input 리셋 검증
        const input = document.querySelector('form#putAddress input[name="recipientName"]');
        expect(input.value).toBe('');

        // loadAddressList 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/list");
    });
});

describe('deleteAddress', () => {

    let form;

    beforeEach(() => {
        document.body.innerHTML = `
          <form id="putAddress">
            <input name="id" value="1" />
            <input name="recipientName" value="홍길동" />
            <input name="zipCode" value="12345" />
            <input name="addressLine1" value="서울시 강남구" />
            <input name="addressLine2" value="101호" />
            <input name="phoneNumber" value="010-1234-5678" />
            <input type="checkbox" name="default" checked />
            <button type="submit">제출</button>
          </form>
          <div id="addressUpdateModal" class="modal"></div>
          <div id="noAddressAlert" class="d-none"></div>
          <div id="addressList" class="row row-cols-1 row-cols-md-2 g-4 d-none"></div>
        `;

        // 모달 관련 메서드 모킹
        window.bootstrap = {
            Modal: {
                getInstance: () => ({
                    hide: jest.fn(),
                }),
            },
        };

        form = document.getElementById("putAddress");
    });


    test('fetch 예외 발생 시', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.reject(new Error('Fetch failed'))
        );

        const submitEvent = new Event("submit");

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("오류가 발생했습니다.");
    });

    test('fetch 결과가 Not Ok', async () => {
        // given
        global.fetch = jest.fn(() =>
            Promise.resolve({
                ok: false,
                json: () => Promise.resolve({ message: "주소 수정 실패" }),
            })
        );

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        expect(alert).toHaveBeenCalledWith("주소 수정 실패");
    });

    test('fetch 결과가 Ok', async () => {
        // given
        global.fetch.mockImplementation((url) => {
            if (url.endsWith('/update')) {
                return Promise.resolve({
                    ok: true,
                    json: () => Promise.resolve({ message: "주소 수정 성공" }),
                });
            }

            if (url.endsWith('/list')) {
                return Promise.resolve({
                    ok: true,
                    json: async () => (
                        [{
                            id: 1,
                        }]
                    ),
                });
            }
        });

        const hideMock = jest.fn();
        const getInstanceMock = jest.fn(() => ({
            hide: hideMock,
        }));
        window.bootstrap = {
            Modal: {
                getInstance: getInstanceMock,
            },
        };

        const mockPrevent = jest.fn();
        const submitEvent = new Event("submit");
        submitEvent.preventDefault = mockPrevent;

        module.setupPutAddressButton()

        // when
        form.dispatchEvent(submitEvent);
        await Promise.resolve();
        await Promise.resolve();

        // then
        // fetch 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/update", expect.objectContaining({
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: expect.any(String),
        }));

        // alert 호출 검증
        expect(alert).toHaveBeenCalledWith("주소 수정 성공");

        // 모달 숨김 검증
        expect(getInstanceMock).toHaveBeenCalledWith(document.getElementById("addressUpdateModal"));
        expect(hideMock).toHaveBeenCalled();

        // input 리셋 검증
        const input = document.querySelector('form#putAddress input[name="recipientName"]');
        expect(input.value).toBe('');

        // loadAddressList 호출 검증
        expect(global.fetch).toHaveBeenCalledWith("/api/address/list");
    });
});