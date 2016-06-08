var ReactiveStore = require('src/ReactiveStore');

describe('ReactiveStore.autorun()', function() {
    var rs1;

    beforeEach(function() {
        rs1 = ReactiveStore();
    });

    it('will not call autorun more than once for multiple gets',() => {
        var spy = jasmine.createSpy().and.callFake(() => {
            rs1.get('myKey');
            rs1.get('myKey');
            rs1.get('myKey');
        });
        rs1.autorun(() => {
            spy();
        });
        expect(spy.calls.count()).toBe(1);
        rs1.set('myKey', 'value');
        expect(spy.calls.count()).toBe(2);
    });

    it('will only trigger autorun on the key being changed', function() {
        var aSpy = jasmine.createSpy().and.callFake(() => rs1.get('a'));
        var bSpy = jasmine.createSpy().and.callFake(() => rs1.get('b'));
        rs1.autorun(aSpy);
        rs1.autorun(bSpy);

        expect([aSpy.calls.count(), bSpy.calls.count()]).toEqual([1,1]);
        rs1.set('a', 1);
        expect([aSpy.calls.count(), bSpy.calls.count()]).toEqual([2,1]);
        rs1.set('b', 1);
        expect([aSpy.calls.count(), bSpy.calls.count()]).toEqual([2,2]);
    });

    it('will notify for a change in value only once per change', function() {
        var value;
        var autorunSpy = jasmine.createSpy().and.callFake(() => value = rs1.get('something'));
        rs1.autorun(autorunSpy);
        expect([autorunSpy.calls.count(), value]).toEqual([1, undefined]);
        rs1.set('something', 'a value');
        expect([autorunSpy.calls.count(), value]).toEqual([2, 'a value']);
        rs1.set('something', 'another');
        expect([autorunSpy.calls.count(), value]).toEqual([3, 'another']);
        rs1.set('something', 'yet another');
        expect([autorunSpy.calls.count(), value]).toEqual([4, 'yet another']);
    });

    it('will react to a change in a deeper value', function() {
        var count = 0;
        var a;

        rs1.autorun(function() {
            a = rs1.get('a');
            count++;
        });

        expect(count).toBe(1);
        rs1.set('a.value', 'something');
        expect([count, a.value]).toEqual([2, 'something']);
        rs1.set('a.value', 'something else');
        expect([count, a.value]).toEqual([3, 'something else']);
    });

    it('will not re-notify if the same value is set', function() {
        var count = 0;
        rs1.autorun(function() {
            rs1.get('a');
            count++;
        });
        expect(count).toBe(1);
        rs1.set('a', 'value');
        expect(count).toBe(2);
        rs1.set('a', 'value');
        expect(count).toBe(2);
        rs1.set('a', 'value');
        expect(count).toBe(2);
        rs1.set('a', 'another');
        expect(count).toBe(3);
    });

    it('should still notify after a clearChildren() call', function() {
        var valSpy = jasmine.createSpy();
        var aSpy = jasmine.createSpy();

        rs1.set('val', {a:1});
        rs1.autorun(function() {
            valSpy();
            rs1.get('val');
        });
        rs1.autorun(function() {
            aSpy();
            rs1.get('val.a');
        });
        expect(valSpy.calls.count()).toBe(1);
        expect(aSpy.calls.count()).toBe(1);
        rs1.set('val.a', 3);
        expect(aSpy.calls.count()).toBe(2);
        expect(valSpy.calls.count()).toBe(2);
        rs1.clearChildren('val');
        return;
        expect(valSpy.calls.count()).toBe(2);
        rs1.set('val', {a:2});
        expect(valSpy.calls.count()).toBe(3);
        expect(aSpy.calls.count()).toBe(3);
    });

    it('should still notify if empty array is stored', function() {
        var spy = jasmine.createSpy();

        rs1.set('arr', [1]);
        rs1.autorun(function() {
            spy();
            rs1.get('arr');
        });
        expect(spy.calls.count()).toBe(1);
        rs1.set('arr', []);
        expect(spy.calls.count()).toBe(2);
    });

    it('should still notify if empty object is stored', function() {
        var spy = jasmine.createSpy();

        rs1.set('obj', {a:1});
        rs1.autorun(function() {
            spy();
            rs1.get('obj');
        });
        expect(spy.calls.count()).toBe(1);
        rs1.set('obj', {});
        expect(spy.calls.count()).toBe(2);
    });

    it('should pass firstRun variable to autorun', function() {
        var spy = jasmine.createSpy();
        rs1.autorun(function(firstRun) {
            spy(firstRun);
            rs1.get('foo');
        });
        rs1.set('foo', 'bar');
        rs1.set('foo', 'baz');
        expect(spy.calls.argsFor(0)).toEqual([true]);
        expect(spy.calls.argsFor(1)).toEqual([false]);
        expect(spy.calls.argsFor(2)).toEqual([false]);
    });

    it('should remember spys that exist on an object even if the object does not exist yet', () => {
        var spy = jasmine.createSpy().and.callFake(() => rs1.get('foo.bar'));
        rs1.autorun(spy);

        expect(spy.calls.count()).toBe(1);
        rs1.set('foo', {bar:1,baz:2, boo: 3});
        expect(spy.calls.count()).toBe(2);
    });

    it('should only notify once for object changes', () => {
        var spy = jasmine.createSpy().and.callFake(() => rs1.get('foo'));
        rs1.autorun(spy);

        expect(spy.calls.count()).toBe(1);
        rs1.set('foo', {bar:1,baz:2, boo: 3});
        expect(spy.calls.count()).toBe(2);
        rs1.set('foo.bar', 10);
        expect(spy.calls.count()).toBe(3);
    });

    it('should only notify once for an array change', () => {
        var spy = jasmine.createSpy().and.callFake(() => rs1.get('foo'));
        rs1.autorun(spy);

        expect(spy.calls.count()).toBe(1);

        rs1.set('foo', [{bar:1},{bar:2},{bar:3}]);
        expect(spy.calls.count()).toBe(2);
    });

    it('should listen to deep property before it exists', () => {
        var spy = jasmine.createSpy().and.callFake(() => rs1.get('foo.bar.baz.boo'));
        rs1.autorun(spy);

        expect(spy.calls.count()).toBe(1);

        rs1.set('foo.bar.baz.boo', 'something');
        expect(spy.calls.count()).toBe(2);
    });

    it('should listen to deep property even if object is stored', () => {
        var spy = jasmine.createSpy().and.callFake(() => rs1.get('foo.bar.baz.boo'));
        rs1.autorun(spy);

        expect(spy.calls.count()).toBe(1);

        rs1.set('foo.bar', {baz: {boo: 10}});
        expect(spy.calls.count()).toBe(2);

        rs1.set('foo.bar.baz.boo', 20);
        expect(spy.calls.count()).toBe(3);
    });
});