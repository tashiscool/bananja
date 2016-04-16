var SignUp = require('components/page/SignUp');
var SignIn = require('components/page/SignIn');

module.exports = class Landing extends PureRenderComponent {


    componentWillMount() {
    }

    render() {
        return (
            <div>
               <SignIn/>
                <SignUp/>
            </div>
        );
    }
};
