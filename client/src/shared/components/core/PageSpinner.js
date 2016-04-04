var count = 0;
var $j = global.jQuery;
module.exports = {

    show() {
        count++ || $j.blockUI();
    },

    hide() {
        count > 0 && (--count || $j.unblockUI());
    },

    reset() {
        count = 0;
        $j.unblockUI();
    }
};

