require('./DataGroup.less');

var DataGroup = module.exports = props => (
    <div className={props.size ? 'data-group data-group--' + props.size : 'data-group'}>
        { props.title && <p className="data-group__title"> {props.title} </p> }
        { props.children }
    </div>
);

DataGroup.Item = props => (<p className="data-group__item">{props.children}</p>);
