var Dict = require('src/Dict');
var Dependency = require('src/Dependency');

describe('Dict', () => {
    var dict;

    beforeEach(() => {
        dict = Dict();
    });

    describe('set()', () => {
        it('should set a value in the store', () => {
            dict.set('test', 'value');
            expect(dict.get('test')).toBe('value');
        });
    });

    describe('addDependencies()', () => {
        it('should add a dependency to the dependency list', () => {
            var dep1 = Dependency();
            var dep2= Dependency();
            dict.addDependency('a.b.c', dep1);
            expect(dict.getDependencies('a.b.c')).toEqual([dep1]);
            dict.addDependency('a.b.c', dep2);
            expect(dict.getDependencies('a.b.c')).toEqual([dep1, dep2]);
        });
    });
});