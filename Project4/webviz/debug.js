$(function () {
	var options = {
        chart: {
            renderTo: 'container',
            type: 'column'
        },
        title: {
            text: 'Requests per second'
        },
        events: {
          load: function() {

                // set up the updating of the chart each second
                var series = this.series[0];
                setInterval(function(){
                var chart = new Highcharts.Chart(options);
                $.getJSON('http://url-to-json-file/index.php', 
                function(jsondata) {
                    options.series[0].data = JSON.parse(jsondata.cpu);
                });
                }, 5000);
       }              
    },
        yAxis: {
            labels: {
                formatter: function() {
                    return this.value + ' %';
                }
            },
            title: {
                text: 'Num requests'
            }
        },
        xAxis: {
            title: {
                text: 'Second'
            }
        },
        tooltip: {
            formatter: function() {
                return 'Second <b>' + this.x + '</b><br>Avg Requests <b>' + this.y + '%</b>';
            }
        },
        legend: {
            enabled: false
        },
        series: [{}]
    };
    
    var chart = new Highcharts.Chart(options);
});