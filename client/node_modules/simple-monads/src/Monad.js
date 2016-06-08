"use strict";

module.exports = class Monad {
    constructor(value) {
        this.value = value;
    }

    join() {
        return this.value instanceof Monad ? this.value.join() : this;
    }

    flatMap(f) {
        return f(this.value);
    }

    get() {
        return this.value;
    }

    bind(fn) {
        return fn(this.value);
    }
};