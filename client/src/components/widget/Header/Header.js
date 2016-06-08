var Link = require('react-router').Link;
var ImgRetina = require('components/core/ImgRetina');
var UserService = require('services/UserService');
var HeaderSignOut = require('components/widget/HeaderSignOut');
var ShowHide = require('shared/group/ShowHide');
require('./header.less');
var drunkrLogo = require(`./logo-drunkr@2x.png`)
var houseIcon = require("components/page/img/icon-house@2x.png")



module.exports = class Header extends PureRenderComponent {

    componentWillMount() {
        this.setState({ menuOpen : false });
        this.registerStoreKey('user.id', 'userId');
    }

    toggleMenu() {
        this.setState({ menuOpen : !this.state.menuOpen });
    }

    render() {
        return (
            <header className="header" role="banner">
                <Grid>
                    <Row>
                    <Col xs={4}>
                        { this.state.userId && headerMenu.call(this) }
                    </Col>
                    <Col xs={4}>
                        <div className="header__logo">
                            <LinkTo to="/">
                                <ImgRetina imgSrc={ drunkrLogo }
                                    alt={gt.gettext(`${global.serverVars.context} logo`)}/>
                            </LinkTo>
                        </div>
                    </Col>
                    <Col xs={4}>
                        <nav className="header__links">
                            { signInLink.call(this) }
                        </nav>
                    </Col>
                    </Row>
                </Grid>
            </header>
        );

        function headerMenu() {
            return  this.state.menuOpen ?
                <div className="header__menu is-open visible-xs">{ menuLinks.call(this) }</div> :
                <div className="header__menu visible-xs"><button className="header__menu-toggle" onClick={ this.toggleMenu.bind(this) }><span className="header__menu-bar" /><span className="header__menu-bars" /></button></div>;
        }

        function menuLinks() {
            return <nav><ul className="header__menu-list">
                <li className="header__menu-link header__menu-link--home" onClick={ this.toggleMenu.bind(this) }>
                    <LinkTo to="/admin/control-panel">
                    <ImgRetina
                        imgSrc={ houseIcon }
                        alt={gt.gettext("Return to admin control panel")}/>
                    </LinkTo>
                </li>
                <li className="header__menu-link" onClick={ this.toggleMenu.bind(this) }><LinkTo to="/admin/customer-lookup">{gt.gettext("Plans & Claims")}</LinkTo></li>
                <li className="header__menu-link" onClick={ this.toggleMenu.bind(this) }><LinkTo to="/admin/reporting">{gt.gettext("Reporting")}</LinkTo></li>
                <li className="header__menu-link" onClick={ this.toggleMenu.bind(this) }><LinkTo to="/admin/marketing">{gt.gettext("Marketing")}</LinkTo></li>
                <li className="header__menu-link" onClick={ this.toggleMenu.bind(this) }><LinkTo to="/admin/training">{gt.gettext("Training")}</LinkTo></li>
                <li className="header__menu-link header__menu-link--close" onClick={ this.toggleMenu.bind(this) }>x</li>
            </ul></nav>;
        }

        function signInLink() {
            return this.state.userId ?
                <HeaderSignOut /> :
                <LinkTo to="/signin" className="btn btn--primary-outline">Sign in</LinkTo>;
        }

    }
}
