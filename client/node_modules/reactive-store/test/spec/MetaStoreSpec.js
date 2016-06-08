var MetaStore = require('src/MetaStore');

describe('MetaStore', () => {
    var store;

    beforeEach(() => {
        store = MetaStore();
    });

    describe('setValue()', () => {
        it('should returned undefined for a non-existent key', () => {
            expect(store.getValue('fake')).not.toBeDefined();
            expect(store.getValue('a.fake')).not.toBeDefined();
        });

        it('should set a value in the base of the tree', () => {
            store.setValue('aKey', 'a value');
            expect(store.getValue('aKey')).toBe('a value');
        });

        it('should set a value deeper in the tree', () => {
            store.setValue('some.deeper.key', 'a value');
            expect(store.getValue('some.deeper.key')).toBe('a value');
        });

        it('should get a sub object', () => {
            store.setValue('some.deeper.key', 'a value');
            expect(store.getValue('some.deeper')).toEqual({key: 'a value'});
        })

        it('should store an object', () => {
            store.setValue('some.key', {foo:1, bar:2});
            expect(store.getValue('some.key.foo')).toBe(1);
        });

        it('should store an array', () => {
            store.setValue('some.deep.array', [1,2,3]);
            expect(store.getValue('some.deep.array')).toEqual([1,2,3]);
        });

        it('should store an object', () => {
            store.setValue('some', {deep: {array: [1,2,3]}});
            expect(store.getValue('some')).toEqual({
                deep: {array: [1,2,3]}
            })
            expect(store.getValue('some.deep.array')).toEqual([1,2,3]);
            expect(store.getValue('some.deep.array.1')).toEqual(2);
        });

        it('should remove extra values when new array is stored', () => {
            store.setValue('arr', [1,2,3,4,5]);
            expect(store.getValue('arr')).toEqual([1,2,3,4,5]);
            store.setValue('arr', [1,2,3]);
            expect(store.getValue('arr')).toEqual([1,2,3]);
        });
    });

    describe('getMeta()', () => {
        it('should return undefined for an unknown key', () => {
            expect(store.getMeta('some.unknown.key', 'fake')).toBe(undefined);
        });

        it('should return the metadata for some given key', () => {
            store.setMeta('some.key', {foo: 'bar', baz:'foo'});
            expect(store.getMeta('some.key', 'foo')).toBe('bar');
            expect(store.getMeta('some.key', 'baz')).toBe('foo');
        });

        it('should return the full metadata object if no name given', () => {
            store.setMeta('some.key', {foo:'bar', baz: 'foo'});
            expect(store.getMeta('some.key')).toEqual({foo: 'bar', baz: 'foo'});
        });
    });

    describe('delete()', () => {
        it('should delete a key', () => {
            store.setMeta('some.deep.key', {foo: 'bar'});
            store.setMeta('some.deep', {foo: 'baz'});
            store.setValue('some.deep.key', 10);
            expect(store.getValue('some.deep.key')).toBe(10);
            store.delete('some.deep.key');
            expect(store.getMeta('some.deep.key')).toBeUndefined();
            expect(store.getMeta('some.deep')).toEqual({foo: 'baz'});
            expect(store.getValue('some.deep.key')).toBeUndefined();
        });
    });

    describe('dump()', () => {
        it('should dump an object representing the values in the store', () => {
            store.setValue('a', {one: 1, two: 2});
            store.setValue('b', [1,2]);
            expect(store.dump()).toEqual({a:{one:1, two:2}, b:[1,2]});
        });
    });

    describe('load()', () => {
        it('should load an object into the store', () => {
            store.load({a: {one: 1, two:2}, b: [1,2]});
            expect(store.getValue('a.one')).toBe(1);
            expect(store.getValue('b.0')).toBe(1);
        });
    });

    describe('getLeaf()', () => {
        it('should create a leaf if one does not exist',() => {
            expect(store.getLeaf('foo.bar')).not.toBeUndefined();
        });
    });

    describe('getLeafIfExists()', () => {
        it('should not create a leaf if one does not exist', () => {
            expect(store.getLeafIfExists('foo.bar')).toBeUndefined();
        });

        it('should return a leaf if one exists', () => {
            store.getLeaf('foo.bar');
            expect(store.getLeafIfExists('foo.bar')).not.toBeUndefined();
        });
    });

    describe('getLeafs()', () => {
        it('should return an empty set for a key that does not exist', () => {
            expect(store.getLeafs('a.b.c')).toEqual([]);
        });

        it('should not return all leafs with values', () => {
            store.setValue('a.b.c', 10);
            store.setValue('a.b.d', 20);
            store.setValue('a.d', 30);
            store.setValue('a.e', undefined);
            var leafs = store.getLeafs('a');
            expect(leafs.length).toBe(3);
            expect(_.pluck(leafs, '__value')).toEqual([10,20,30]);
        });

        it('should return an empty array if passed a leaf', () => {
            store.setValue('a.b.c', 10);
            expect(store.getLeafs('a.b.c')).toEqual([]);
        });
    });
});