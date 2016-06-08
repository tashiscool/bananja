var ReactiveStore = require('src/ReactiveStore');


describe('ReactiveStore.debug', function() {
    var rs;
    var logSpy;

    beforeEach(function() {
        rs = ReactiveStore();
        logSpy = spyOn(console, 'log');
    });

    it('should not show get debug by default', function() {
        rs.get('a');
        expect(logSpy).not.toHaveBeenCalled();
    });

    it('should not show set debug by default', function() {
        rs.set('a', 1);
        expect(logSpy).not.toHaveBeenCalled();
    });

    it('should show get debug only when turned on', function() {
        rs.debug.on();
        rs.get('a');
        expect(logSpy).toHaveBeenCalledWith('get(a)');
        expect(logSpy.calls.count()).toBe(1);

        rs.debug.off();
        rs.get('a');
        expect(logSpy.calls.count()).toBe(1);
    });

    it('should show set debug only when turned on', function() {
        rs.debug.on();
        rs.set('a',1);
        expect(logSpy).toHaveBeenCalledWith('set(a, 1)');
        expect(logSpy.calls.count()).toBe(1);

        rs.debug.off();
        rs.set('a', 1);
        expect(logSpy.calls.count()).toBe(1);
    });

});