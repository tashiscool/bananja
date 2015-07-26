$(document).ready( function(){ 
	var data = {
    labels: ["2010", "2011", "2012", "2013", "2014"],
    datasets: [
        {
            label: "My First dataset",
            fillColor: "rgba(220,220,220,0.5)",
            strokeColor: "rgba(220,220,220,0.8)",
            highlightFill: "rgba(220,220,220,0.75)",
            highlightStroke: "rgba(220,220,220,1)",
            data: [65, 68, 75, 85, 80]
        },
        {
            label: "My Second dataset",
            fillColor: "rgba(151,187,205,0.5)",
            strokeColor: "rgba(151,187,205,0.8)",
            highlightFill: "rgba(151,187,205,0.75)",
            highlightStroke: "rgba(151,187,205,1)",
            data: [50, 60, 72, 72, 85]
        }
		]
	};
	var ctx = document.getElementById("compareChart").getContext("2d");
	var barChart = new Chart(ctx).Line(data, {
		responsive:true	
	});
	
	var data2 =[ {
        value: 300,
        color:"#dd0000",
        highlight: "#FF5A5E",
        label: "Interpersonal"
    },
    {
        value: 50,
        color: "#008000",
        highlight: "#5AD3D1",
        label: "Technical"
    },
    {
        value: 100,
        color: "#FFD500",
        highlight: "#FFC870",
        label: "Management"
    }];
	var ctx2 = document.getElementById("compareChartSkills").getContext("2d");
	var pieChart = new Chart(ctx2).Doughnut(data2, {
		responsive:true		
	});
	
	var data3 =[ {
        value: 100,
        color:"#dd0000",
        highlight: "#FF5A5E",
        label: "Interpersonal"
    },
    {
        value: 120,
        color: "#008000",
        highlight: "#5AD3D1",
        label: "Technical"
    },
    {
        value: 140,
        color: "#FFD500",
        highlight: "#FFC870",
        label: "Management"
    }];
	var ctx3 = document.getElementById("compareChartSkills2").getContext("2d");
	var pieChart2 = new Chart(ctx3).Doughnut(data3, {
		responsive:true		
	});
});
