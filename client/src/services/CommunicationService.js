var AppService = require('services/AppService');
const APP_TIMEOUT = 1600000;

var commandProcessor = {
    process: resp => {
        return commandProcessor[resp.cmd] ? commandProcessor[resp.cmd](resp.data) : resp.data;
    },
    setValue: data => {
        RS.set(data.key, data.value);
    },
    noAuthFound: () => {
        redirectBrowser('/no-auth-signin');
    },
    authTimedOut: () => {
        redirectBrowser('/timeout-signin');
    },
    redirectBrowser: data => {
        commandProcessor.setValue(data);
        redirectBrowser('/admin/dashboard');
    }
};

var resetTimeout = (function() {
    var timeoutId;
    return function resetTimeout() {
        timeoutId && timeoutId.cancel();
        timeoutId = _.debounce(() => {
            redirectBrowser('/signin');
        }, APP_TIMEOUT, {trailing: true});
        timeoutId();
    }
}());

function redirectBrowser(location) {
    window.location.href = AppService.getUrl(location);
}


var serverRequest = (url, method, data) => {
    resetTimeout();
    return new Promise(doRequest);

    function doRequest(resolve, reject) {
        $j.ajax(
            {
                url: url,
                type: method,
                cache: false,contentType:"application/json; charset=utf-8",
                dataType:"json",
                data: method === "GET"? data : JSON.stringify(data),

                success: function (data, textStatus, jqXHR) {
                    resolve(data);
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    reject(errorThrown);
                }
            }
        )
    }
};
var generateURL = function(svc, action, template){
    var keys = Object.keys(template);
    var templatesParams = _.reduce(keys, function(memo, it) {
        return memo + `/${it}/${template[it]}`;
    }, "");
    return `/${svc}/${action}`+templatesParams;
};

var that = module.exports = {
    // nicely divides the url into svc and action, ie. auth/signin -> AuthenticationApi.authenticateSignIn
    send: ({svc, action, method='GET', data={}, template={}}) =>
        //executes serverRequest, calling some controller, then getting back 1 or more CommandResponse obj
        serverRequest(AppService.getUrl(generateURL(svc,action,template)), method, data)
        //executes commandProcessor.process against each response sent
            .then(resp => resp.map(commandProcessor.process))

};

['get', 'post', 'delete', 'put'].forEach(method => that[method] = conf => that.send(_.extend({method: method}, conf)));
