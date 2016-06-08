"use strict";
var Monad = require('./Monad');
var Either = require('./Either')
var R = require('ramda');

class Maybe extends Monad {
    static of(a) {
        return a !== null && a !== undefined ? new Just(a) : new Nothing();
    }

    isNothing() {
        return false;
    }

    isJust() {
        return false;
    }

    static lift(fn) {
        return R.compose(Maybe.of, fn);
    }

    toEither() {
        return Either.of(this.value);
    }
};

class Just extends Maybe {

    map(f) {
        return Maybe.of(f(this.value));
    }
    
    getOrElse() {
        return this.value;
    }

    isJust() {
        return true;
    }

    toString() {
        return `Maybe.Just(${this.value})`;
    }
}

class Nothing extends Maybe {
    map(f) {
        return this; // noop (mapping over nothing)
    }

    getOrElse(other) {
        return other;
    }

    isNothing() {
        return true;
    }

    toString() {
        return 'Maybe.Nothing';
    }
}

module.exports = {
    Maybe: Maybe,
    Just: Just,
    Nothing: Nothing
};