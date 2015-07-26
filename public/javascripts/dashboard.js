$(document).ready( function(){ 
	
	var data2 =[ {
        value: 340,
        color:"#dd0000",
        highlight: "#FF5A5E",
        label: "Unavailable"
    },
    {
        value: 40,
        color: "#008000",
        highlight: "#5AD3D1",
        label: "Available"
    },
    {
        value: 80,
        color: "#FFD500",
        highlight: "#FFC870",
        label: "Pending"
    }];
	var ctx2 = document.getElementById("budgetChart").getContext("2d");
	var pieChart = new Chart(ctx2).Pie(data2, {
		responsive:true		
	});
	
});
