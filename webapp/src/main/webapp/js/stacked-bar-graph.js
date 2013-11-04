var margin = {top: 20, right: 200, bottom: 30, left: 100},
    width = 960 - margin.left - margin.right,
    height = 500 - margin.top - margin.bottom;

var x = d3.scale.ordinal()
.rangeRoundBands([0, width], .1);

var y = d3.scale.linear()
.rangeRound([height, 0]);

var color = d3.scale.ordinal()
.range(["#C52D2D", "#FC9E48", "#FAF357", "#4FC44A"]);

var xAxis = d3.svg.axis()
.scale(x)
.orient("bottom");

var yAxis = d3.svg.axis()
.scale(y)
.orient("left")
.tickFormat(d3.format(".0%"));

var svg = d3.select("#bar-graph").append("svg")
.attr("width", width + margin.left + margin.right)
.attr("height", height + margin.top + margin.bottom)
.append("g")
.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

d3.json("graphsservice/percentages/", function(data) {

color.domain(d3.keys(data[0]).filter(function(key) { return key !== "questionName"; }));

data.forEach(function(d) {
    var y0 = 0;
    d.ratings = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
d.ratings.forEach(function(d) { d.y0 /= y0; d.y1 /= y0; });
});

data.sort(function(a, b) { return b.ratings[0].y1 - a.ratings[0].y1; });

x.domain(data.map(function(d) { return d.questionName; }));

svg.append("g")
.attr("class", "x axis")
.attr("transform", "translate(0," + height + ")")
.call(xAxis);

svg.append("g")
.attr("class", "y axis")
.call(yAxis);

var questionName = svg.selectAll(".questionName")
.data(data)
.enter().append("g")
.attr("class", "questionName")
.attr("transform", function(d) { return "translate(" + x(d.questionName) + ",0)"; });

questionName.selectAll("rect")
.data(function(d) { return d.ratings; })
.enter().append("rect")
.attr("width", x.rangeBand())
.attr("y", function(d) { return y(d.y1); })
.attr("height", function(d) { return y(d.y0) - y(d.y1); })
.style("fill", function(d) { return color(d.name); });

var legend = svg.select(".questionName:last-child").selectAll(".legend")
.data(function(d) { return d.ratings; })
.enter().append("g")
.attr("class", "legend")
.attr("transform", function(d) { return "translate(" + x.rangeBand() / 2 + "," + y((d.y0 + d.y1) / 2) + ")"; });

legend.append("line")
.attr("x2", 10);

legend.append("text")
.attr("x", 13)
.attr("dy", ".35em")
.text(function(d) { return d.name; })
});