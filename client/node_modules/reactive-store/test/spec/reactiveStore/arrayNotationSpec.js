var ReactiveStore = require('src/ReactiveStore');

describe('array notation keys', () => {
    var rs;

    beforeEach(() => {
        rs = ReactiveStore();
    });

    describe('set()', () => {
        it('should accept array notation', () => {
window.store = rs.raw();
            rs.set('a[1].b.c[2]', 10);
            expect(rs.get('a.1.b.c.2')).toBe(10);
        });
    });

    describe('get()', () => {
        it('should accept array notation', () => {
            rs.set('a.1.b.c.2', 11);
            expect(rs.get('a[1].b.c[2]')).toBe(11);
        });
    });
});