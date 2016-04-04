var CommunicationService = require('services/CommunicationService');
var AppService = require('services/AppService');

var GradesService =  module.exports = {


    addGrade(gradeObj) {
        return CommunicationService.post({svc: 'user', action:'grade', data: gradeObj});
    },

    getChart(userId, sectionId) {
        return CommunicationService.get({svc: 'user', action: 'report', template: {user: userId, section: sectionId}});
    }



};

GradesService.addGradeObj = (props) => {
    return _.defaults({}, props, {
        student: undefined,
        studentId: undefined,
        section: undefined,
        term: undefined,
        homeworkGrades: undefined,
        quizGrades: undefined,
        midTermExam: undefined,
        labGrades: undefined,
        finalExam: undefined,
        weightedTotal: undefined
    });
};
