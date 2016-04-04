var R = require('react-router');
var App = require('components/App');
var AdminRoutes = require('routes/AdminRoutes');
var createHashHistory = require('history/lib/createHashHistory');
var createBrowserHistory = require('history/lib/createBrowserHistory');

module.exports = class Router extends Component {
    render() {
        var history = window.history ? createBrowserHistory() : createHashHistory();

        return (
            <R.Router history={history}>
                <R.Route path={ '/' } component={App}>
                    <R.IndexRoute component={require('components/page/SignIn')} onEnter={redirectToAdmin}/>
                    <R.Route path={ ctx('/signin') } component={require('components/page/SignIn')} onEnter={redirectToAdmin}/>
                    {AdminRoutes()};

                    <R.Route path={ ctx('/*') } component={require('components/page/Error')}/>
                </R.Route>
            </R.Router>
        );

        function ctx(path) {
            return AppService.getUrl(path);
        }
    }


};

function redirectToAdmin(nextState, replaceState) {
    RS.get('user.id') && replaceState({}, AppService.getUrl('/admin/dashboard'));
}


