var Form = require('shared/group/InputForm');
var Alert = require('shared/core/Alert');
var UserService = require('services/UserService');
var LinkTo = require('components/core/LinkTo');

module.exports = class SignUp extends PureRenderComponent {


    componentWillMount() {
        this.registerStoreKey('signup.alertBar');

    }


    signup() {
        var obj = RS.get("signup-form.values");
        this.refs.signupForm.isValid() && UserService.signUp(obj.username, obj.password, obj.phone).then(UserService.signupComplete);

    }

    render() {
        var regex = /^[-a-z0-9~!$%^&*_=+}{\'?]+(\.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(\.[-a-z0-9_]+)*\.(aero|arpa|biz|com|coop|edu|gov|upfo|upt|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,5})?$/i;
        return (
            <div>
                { _.size(this.state.alertBar) > 0  &&
                <Alert type="danger" imgSrc={ require('components/page/img/icon-info.png') } imgAlt="upfo Icon"
                       onClose={ () => {RS.set('signup.alertBar', [])} }>{this.state.alertBar}</Alert> }
                    <Form ref="signupForm" rsKey="signup-form">
                        <Form.InputText name="username" label={gt.gettext("Email Address")}
                                        customValidator={value => regex.test(value) ? '' : 'Please enter a valid email address'}/>
                        <Form.InputText name="phone" label={gt.gettext("Phone Number")}
                                        customValidator={value => /^[0-9]{10}$/i.test(value) ? '' : 'Please enter a valid phone address'}/>
                        <Form.InputText name="password" label={gt.gettext("Password")}
                                        type="password"
                                        customValidator={value => /(?=.*[0-9])(?=.*[a-zA-Z])(.+){7,}/i.test(value) ? '' : 'Please enter a valid password'}
                                        helpText={gt.gettext("Note: Mupimum 7 characters with at least 1 number and 1 letter.")}/>
                    </Form>
                    <Form.BtnPrimary onClick={this.signup.bind(this)}>{gt.gettext("Sign Up")}</Form.BtnPrimary>
                <LinkTo to="/password/reset">{gt.gettext('Forgot password?')}</LinkTo>
            </div>
        );
    }
};
