var ReactiveStore = require('src/ReactiveStore');

describe('ReactiveStore.nonReactive', function() {
    var rs;
    var spy;

    beforeEach(function() {
        rs = ReactiveStore();
        spy = jasmine.createSpy().and.callFake(function() {
            return rs.get('a');
        });
    });


    it('testing spy to ensure it works', function() {
        rs.autorun(spy);
        expect(spy.calls.count()).toBe(1);
        rs.set('a',1);
        expect(spy.calls.count()).toBe(2);

    });

    it('should create a non-reactive context within a reactive one', function(){
        rs.autorun(function() {
            rs.nonReactive(spy);
        });
        expect(spy.calls.count()).toBe(1);
        rs.set('a',1);
        expect(spy.calls.count()).toBe(1);
    });

});