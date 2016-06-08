var ReactiveStore = require('src/ReactiveStore');
var _ = require('lodash');

describe('ReactiveStore.set', function() {
    var rs1, rs2;

    beforeEach(function() {
        rs1 = ReactiveStore();
        rs2 = ReactiveStore();
    });


    it('should set a value in the store', function() {
        rs1.set('testing', 'my value');
        expect(rs1.get('testing')).toBe('my value');
    });

    it('should be able to set different values on different stores', function() {
        rs1.set('testing', 'a value');
        rs2.set('testing', 'different');
        expect(rs1.get('testing')).toBe('a value');
        expect(rs2.get('testing')).toBe('different');
    });

    it('should be able to store values with "." in their names', function() {
        rs1.set('some.value', 'a value');
        expect(rs1.get('some.value')).toBe('a value');
    });

    it('should be able to handle deep values with already existing values', function() {
        rs1.set('a.value', 'a value');
        rs1.set('a.another', 'another');
        rs1.set('a.something', 'something');
        expect(rs1.get('a.value')).toBe('a value');
        expect(rs1.get('a.another')).toBe('another');
        expect(rs1.get('a.something')).toBe('something');
        expect(rs1.get('a')).toEqual({value: 'a value', another: 'another', something: 'something'});
    });

    it('should be able to handle an array that is not set up front', () => {
        rs1.set('arr.0', 1);
        rs1.set('arr.1', 2);
        expect(_.isArray(rs1.get('arr'))).toBe(true);
    });

    it('should be able to handle an object', function() {
        var obj = {
            'value': 'value',
            'a.value': 'another',
            object: {
                something: 'something'
            }
        }
        rs1.set('o', obj);
        expect(rs1.get('o')).toEqual({
            value: 'value',
            a: {value: 'another'},
            object: {something: 'something'}
        });
    });

    it('should be able to set an array and get an array item', function() {
        rs1.set('arr', [1,2,3]);
        expect(rs1.get('arr.1')).toBe(2);
    });

    it('should return an array when array stored', function() {
        rs1.set('arr', [0,1,2]);
        expect(Array.isArray(rs1.get('arr'))).toBe(true);
    });

    it('should return array of objects if array of objects stored', function() {
        rs1.set('objArr', [{a:1},{a:2},{a:3}]);
        expect(rs1.get('objArr.0').a).toBe(1);
        expect(rs1.get('objArr.0.a')).toBe(1);
        expect(_.isArray(rs1.get('objArr'))).toBe(true);
    });

    it('should remove keys if an array is being updated', function() {
        rs1.set('arr', [1,2,3]);
        expect(rs1.get('arr')).toEqual([1,2,3]);
        rs1.set('arr', [1,2]);
        expect(rs1.get('arr')).toEqual([1,2]);
    });

    it('should be able to store an empty array', function() {
        rs1.set('arr', []);
        expect(rs1.get('arr')).toEqual([]);
        rs1.set('arr',[1,2]);
        expect(rs1.get('arr')).toEqual([1,2]);
        rs1.set('arr', []);
        expect(rs1.get('arr')).toEqual([]);
    });

    it('should be able to store an emtpy object', function() {
        rs1.set('obj', {});
        expect(rs1.get('obj')).toEqual({});
    });

    it('should be able to store an object with numeric keys', function() {
        var obj = {'1':'One', '2':'Two'};
        rs1.set('obj', obj);
        expect(rs1.get('obj')).toEqual(obj);
    });

    it('should allow you to set undefined on a key set with a string', () => {
        rs1.set('obj', 'testing');
        rs1.set('obj', undefined);
        expect(rs1.get('obj')).not.toBeDefined();
    });

    it('should allow you to set undefined on a key with an object', () => {
        rs1.set('obj', {something: 1});
        rs1.set('obj', undefined);
        expect(rs1.get('obj')).not.toBeDefined();
    });

    it('should invalidate only one key of an object when set undefined', () => {
        rs1.set('obj', {something:1, nothing: undefined});
        expect(rs1.get('obj')).toEqual({something: 1});

        rs1.set('arr', [{something: 1, nothing: undefined}]);
        expect(rs1.get('arr.0')).toEqual({something: 1});
    });

    it('should allow an object with key "length"', () => {
        rs1.set('obj', {length:10});
        expect(rs1.get('obj')).toEqual({length: 10});
    });
    
    it('should be able to set a numeric key to a value', () => {
        rs1.set('111', 'xxx');
        expect(rs1.get('111')).toBe('xxx');
    });

    it('should be able to set a number key to undefined', () => {
        rs1.set('111', {});
        rs1.set('111', undefined);
        expect(rs1.get('111')).toBe(undefined);
    });
});