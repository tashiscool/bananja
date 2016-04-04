require('components/style/app.less');
global.gt = require('services/Gettext');

global.App = module.exports = class App extends Component {

    render() {
        App.history = this.props.history;
        return (
            <div className={ global.serverVars.context } role="application">
                <main role="main">
                    { this.props.children }
                </main>
            </div>
        );
    }
};

_.extend(App, {
    language: global.serverVars.language,
    context: global.serverVars.context
});

gt.init({
    'en-us': require('messages/en-us.po')
}[App.language]);






App.goto = path => App.history.push(AppService.getUrl(path));
App.gotoPartial = path => () => App.goto(path);



global.storeState = (name = 'current') => sessionStorage.setItem(`state-${name}`, JSON.stringify(RS.dump()));
global.clearState = (name = 'current') => sessionStorage.removeItem(`state-${name}`);
global.listStates = () => Object.keys(sessionStorage).filter(k => /^state-/.test(k));
global.useState = (name) => {
    var state = sessionStorage.getItem(`state-${name}`);
    state ? useState() : console.log(`state not found: ${name}`);

    function useState() {
        sessionStorage.setItem('state-current', state);
    }
};

var state = sessionStorage.getItem('state-current');
state && RS.load(JSON.parse(state));