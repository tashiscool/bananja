require('shared/core/style/card.less');

var Card = module.exports = props => {
    return <div className={`card ${ props.optionClass && props.optionClass}`}  {...props}>{props.children}</div>
};