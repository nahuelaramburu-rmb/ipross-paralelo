import * as authentication from '../authentication';

function mockFetch(data) {
    return jest.fn().mockImplementation(() =>
        Promise.resolve({
            ok: true,
            json: () => data,
        })
    );
}

describe('user authentication', () => {
    afterEach(() => {
        jest.resetAllMocks();
    });

    it('do login', async () => {
        const credentials = {
            username: 'AAA',
            password: 'BBB',
        };

        const response = { access_token: 'aaa', refresh_token: 'bbb', token_type: 'ccc', expires_in: 500 };

        global.fetch = mockFetch(response);

        return authentication.authenticateUser(credentials).then((resp) => {
            expect(resp).toEqual(response);
        });
    });

    it('login error', async () => {
        const credentials = {
            username: 'AAA',
            password: 'BBB',
        };

        const response = { errorCode: 404, message: 'Not Found' };

        global.fetch = mockFetch(response);

        return authentication.authenticateUser(credentials).then((resp) => {
            expect(resp).toEqual(response);
        });
    });
});
