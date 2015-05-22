var margin = {top: 0, right: 0, bottom: 0, left: 0};
var width = $('#chart').width();
var height = $('#chart').height();

var color = d3.scale.category20();

var treemap = d3.layout.treemap()
    .size([width, height])
    .sticky(true)
    .value(function(d) { return d.size; });

var div = d3.select('#chart');

function createTreemapChart(jsonData) {
	createVisualization(jsonData);
}
function createVisualization(root){
	
	var node = div.datum(root).selectAll(".node")
      .data(treemap.nodes)
    .enter().append("div")
      .attr("class", "node")
      .call(position)
      .style("background", function(d) { return d.children ? ((d.backgroundColor) ? d.backgroundColor : color(d.name)) : null; })
      .style("color", function(d) { return ((d.fontColor) ? d.fontColor : undefined); })
      .style("border", function(d) { return ((d.fontColor) ? '1px solid ' + d.fontColor : undefined); })
      .text(function(d) { return d.children ? null : d.name; })
      .attr("title", function(d) { return d.name + "\n" + d.size + " times"; });
};

function position() {
  this.style("left", function(d) { return d.x + "px"; })
      .style("top", function(d) { return d.y + "px"; })
      .style("width", function(d) { return Math.max(0, d.dx - 1) + "px"; })
      .style("height", function(d) { return Math.max(0, d.dy - 1) + "px"; });
}