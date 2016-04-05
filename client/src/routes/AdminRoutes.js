var R = require('react-router');

module.exports = () => (
    <R.Route path={ ctx('/admin') } onEnter={redirectToSignin}>
        <R.Route path={ 'dashboard' } component={require('components/page/SignIn')}/>
    </R.Route>
);

function ctx(path) {
    return AppService.getUrl(path);
}

function redirectToSignin(nextState, replaceState) {
    if ( RS.get('user.id') === undefined ) {
        replaceState({
            nextPathname: nextState.location.pathname
        }, AppService.getUrl('/signin'));
    }
}

