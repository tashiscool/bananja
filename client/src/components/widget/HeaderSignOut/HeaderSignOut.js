var Link = require('shared/core/Link');
var ImgRetina = require('components/core/ImgRetina');
var ShowHide = require('shared/group/ShowHide');
require('./header-signout.less');
var icon_signout = require('./icon-signout@2x.png')
module.exports = class HeaderSignOut extends PureRenderComponent {

    componentDidMount(){
        this.setState({ show: false});
    }

    toggle(){
        this.setState({ show: !this.state.show });
    }

    render() {
        return <div className="header__signout">
            <div className="header__signout-icon" role="button" onClick={this.toggle.bind(this)}>
                <ImgRetina imgSrc={ icon_signout } alt={gt.gettext("Sign out of the application")} />
            </div>
            <ShowHide show={this.state.show}>
                <ul className="header__signout-list">
                  {/*<li className="header__signout-item"><Link onClick={ changePassword.bind(this) }>{ gt.gettext("Change Password") }</Link></li>*/}
                  <li className="header__signout-item"><Link onClick={ signOut.bind(this) }>{ gt.gettext("Sign Out") }</Link></li>
                </ul>
            </ShowHide>
        </div>;

        function signOut() {
            UserService.logout();
            App.goto('/');
        }

        function changePassword() {
            return true;
        }
    }
};
