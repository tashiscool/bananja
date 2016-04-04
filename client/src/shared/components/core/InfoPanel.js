var Component = require('Component');
require('shared/core/style/infoPanel.less');

var InfoPanel = module.exports = class InfoPanel extends Component {
    render() {
        return (
            <div className="info-panel" {...this.props}>
                {this.props.children}
            </div>
        )
    }
};

InfoPanel.Header = class InfoPanelHeader extends Component {
    render() {
        return (
            <div className="info-panel__header">
                {this.props.children}
            </div>
        )
    }
};

InfoPanel.Body = class InfoPanelBody extends Component {
    render() {
        return (
            <div className="info-panel__body">
                {this.props.children}
            </div>
        )
    }
};

InfoPanel.Footer = class InfoPanelFooter extends Component {
    render() {
        return (
            <div className="info-panel__footer">
                {this.props.children}
            </div>
        )
    }
};
