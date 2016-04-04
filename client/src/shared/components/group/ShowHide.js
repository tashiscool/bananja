

module.exports = class ShowHide extends PureRenderComponent {
    render() {
        return <div>{this.props.show && this.props.children}</div>
    }
};
