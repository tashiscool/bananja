var ReactiveStore = require('src/ReactiveStore');


var _ = require('lodash');

describe('ReactiveStore.get()', function() {
    var rs1;

    beforeEach(function() {
        rs1 = ReactiveStore();
    });

    it('should be able to get value with simple key', function() {
        rs1.set('something', 'value');
        expect(rs1.get('something')).toBe('value');
    });

    it('should be able to get a value with a complex key', function() {
        rs1.set('a.value', 'some value');
        expect(rs1.get('a.value')).toBe('some value');
    });

    it('should be able to get a deep value at a shallower level', function() {
        rs1.set('a.valueA', 'some value');
        rs1.set('a.valueB', 'another');
        expect(rs1.get('a')).toEqual({valueA: 'some value', valueB: 'another'});
    });

    it('should return array  if array stored', function() {
        rs1.set('arr', [1,2,3]);
        expect(_.isArray(rs1.get('arr'))).toBe(true);
        expect(rs1.get('arr')).toEqual([1,2,3]);
    });
});