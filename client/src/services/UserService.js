var CommunicationService = require('services/CommunicationService');
var AppService = require('services/AppService');

window.UserService =  module.exports = {
    store() {
        global.sessionStorage.setItem('user', JSON.stringify(RS.get('user')));
    },

    load() {
        var userJson = sessionStorage.getItem('user');
        userJson && RS.set('user', JSON.parse(userJson));
    },

    logout() {
        RS.set('user.id', undefined);
        this.store();
    },

    authenticate(username, password) {
        return CommunicationService.post({svc: 'auth', action: 'signin', data: {username: username, password: password}});
    },
    authenticate(username, password, phone) {
        return CommunicationService.post({svc: 'auth', action: 'signin', data: {username: username, password: password, phone: phone}});
    },

    signInComplete() {
        var user = RS.get('user');
        if(user === undefined || user.role === undefined){
            RS.set("signin.alertBar", gt.gettext("Sign in attempt was unsuccessful, please try again"));
        } else if(user.role === "disabled") {
            App.goto("/signin/disabled");
        } else {
            UserService.store();
            App.goto('/admin/dashboard');
        }
    }
};
