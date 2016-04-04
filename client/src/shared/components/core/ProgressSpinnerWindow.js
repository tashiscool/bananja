var Component = require('Component');

module.exports = class ProgressSpinnerWindow extends Component {
    render() {
        return this.props.isLoading ? spinner() : <div>{this.props.children}</div>;

        function spinner() {
            return (
                <div className="progress-spinner progress-spinner--window" data-component="progressSpinnerWindow">
                    <div className="progress-spinner_overlay"></div>
                    <div className="progress-spinner_content"></div>
                </div>
            );
        }
    }
};