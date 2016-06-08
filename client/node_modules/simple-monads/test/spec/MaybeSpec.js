var Maybe = require('../../lib/simple-monads').Maybe;

describe('Maybe monad', () => {
    describe('of()', () => {
        it('should return Nothing if value is undefined', () => {
            expect(Maybe.of(undefined).toString()).toBe('Maybe.Nothing');
        });

        it('should return Nothing if value is null', () => {
            expect(Maybe.of(null).toString()).toBe('Maybe.Nothing');
        });

        it('should return a Just wrapped value', () => {
            expect(Maybe.of(10).toString()).toBe('Maybe.Just(10)');
        });
    });

    describe('lift()', () => {
        it('returns a function that returns a maybe monad given a function', () => {
            var spy = jasmine.createSpy().and.callFake(v => v*2);
            var maybeFn = Maybe.lift(spy);

            var ret = maybeFn(10);
            expect(ret.toString()).toBe('Maybe.Just(20)');
            expect(ret.isJust()).toBe(true);
            expect(spy.calls.count()).toBe(1);
        });
    });

    describe('toEither()', () => {
        it('returns a Right if there is a value', () => {
            expect(Maybe.of(10).toEither().isRight()).toBe(true);
        });

        it('returns a Left if there is no value', () => {
            expect(Maybe.of().toEither().isLeft()).toBe(true);
        });
    });

    describe('map()', () => {
        it('should not run if the maybe is undefined or null', () => {
            var spy = jasmine.createSpy();
            Maybe.of(undefined).map(spy);
            Maybe.of(null).map(spy);
            expect(spy).not.toHaveBeenCalled();
        });

        it('should run if the maybe is not undefined or null', () => {
            var spy = jasmine.createSpy();
            Maybe.of('xx').map(spy);
            expect(spy).toHaveBeenCalledWith('xx');
        });

        it('should chain maps returning a maybe', () => {

            var spy1 = jasmine.createSpy().and.returnValue('yy');
            var spy2 = jasmine.createSpy().and.returnValue('zz');
            var ret = Maybe.of('xx').map(spy1).map(spy2);
            expect(spy1).toHaveBeenCalledWith('xx');
            expect(spy2).toHaveBeenCalledWith('yy');
            expect(ret.toString()).toBe('Maybe.Just(zz)');

            spy1 = jasmine.createSpy().and.returnValue(undefined);
            spy2 = jasmine.createSpy();
            var ret = Maybe.of('xx').map(spy1).map(spy2);
            expect(spy1).toHaveBeenCalledWith('xx');
            expect(spy2).not.toHaveBeenCalled();
            expect(ret.toString()).toBe('Maybe.Nothing');
        });
    });
});