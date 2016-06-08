var Either = require('../../lib/simple-monads').Either;
describe('Either monad', () => {

    describe('of()', () => {
        it('should assign left if value is null or undefined', () => {
            expect(Either.of(null).isLeft()).toBe(true);
            expect(Either.of(undefined).isLeft()).toBe(true);
        });

        it('should assign right if value is not null or undefined', () => {
            expect(Either.of(10).isRight()).toBe(true);
        });
    });

    describe('get()', () => {
        it('should return the value of a right', () => {
            expect(Either.of(10).get()).toBe(10);
        });

        it('should throw an exception if trying to get the value of the left', () => {
            expect(Either.of(undefined).get).toThrow();
        });
    })

    describe('map()', () => {
        it('should call passed returning right if return is not null or undefined', () => {
            var spy = jasmine.createSpy().and.returnValue(20);
            expect(Either.of(10).map(spy).isRight()).toBe(true);
            expect(spy).toHaveBeenCalledWith(10);
        });

        it('should return left if result of function is undefined', () => {
            var spy = jasmine.createSpy();
            expect(Either.of(10).map(spy).isLeft()).toBe(true);
            expect(spy).toHaveBeenCalledWith(10);
        });
    });

    describe('getOrElse()', () => {
        it('should return the the value of the either if right', () => {
            expect(Either.of(10).getOrElse(20)).toBe(10);
        });

        it('should return the other if the either is left', () => {
            expect(Either.of(undefined).getOrElse(20)).toBe(20);
        });
    });

    describe('orElse()', () => {
        it('should return the value if right', () => {
            var spy = jasmine.createSpy();
            expect(Either.of(10).orElse(spy).get()).toBe(10);
            expect(spy).not.toHaveBeenCalled();
        });

        it('should run the passed function if left', () => {
            var spy = jasmine.createSpy().and.returnValue(10);
            expect(Either.of(undefined).orElse(spy).get()).toBe(10);
            expect(spy).toHaveBeenCalled();
        })
    });


});