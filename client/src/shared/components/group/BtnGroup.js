
require('shared/group/style/btnGroup.less');

module.exports = class BtnGroup extends PureRenderComponent {
    render() {
        return (
            <div className={ 'btn-group btn-group--center ' + getClass.call(this) }>
                <div className="btn-group__list">
                    {React.Children.map(this.props.children, child => <div className="btn-group__btn">{child}</div>)}
                </div>
            </div>
        )

        function getClass(){
            return this.props.type ? 'btn-group--' + this.props.type : 'btn-group--form-footer';
        }
    }
}