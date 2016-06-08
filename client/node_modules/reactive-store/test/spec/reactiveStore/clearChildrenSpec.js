var ReactiveStore = require('src/ReactiveStore');


describe('ReactiveStore.clearChildren()', function() {
    var rs1;

    beforeEach(function() {
        rs1 = ReactiveStore();
    });

    it('should clear child keys from the store', function() {
        rs1.set('obj', {a:1,b:2,c:3});
        rs1.clearChildren('obj');
        expect(rs1.get('obj')).toEqual({});
    });

    it('should clear an array from the store', function() {
        rs1.set('arr', [1,2,3]);
        rs1.clearChildren('arr');
        expect(rs1.get('arr')).toEqual([]);
    });
});