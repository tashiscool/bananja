require('babel-polyfill');
require('style/bootstrap-3.3.5-dist/css/bootstrap.css');
global.React = require('react');
var ReactDom = require('react-dom');
var Router = require('components/Router');
var UserService = require('services/UserService');

UserService.load();


$j(() => ReactDom.render(<Router/>, $j('#container').get(0)));
