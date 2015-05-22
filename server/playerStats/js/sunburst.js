// Dimensions of sunburst.
var width = $('#chart').width();
var height = $('#chart').height() - 40;
var radius = Math.min(width, height) / 1.6;

// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
var b = {
  w: 120, h: 30, s: 3, t: 10
};

// Total size of all segments; we set this later, after loading the data.
var totalSize = 0; 

var vis = d3.select("#chart").append("svg:svg")
    .attr("width", width)
    .attr("height", height)
    .append("svg:g")
    .attr("id", "container")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

var partition = d3.layout.partition()
    .size([2 * Math.PI, radius * radius])
    .value(function(d) { return d.size; });

var arc = d3.svg.arc()
    .startAngle(function(d) { return d.x; })
    .endAngle(function(d) { return d.x + d.dx; })
    .innerRadius(function(d) { return Math.sqrt(d.y); })
    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });

// Use d3.text and d3.csv.parseRows so that we do not need to have a header
// row, and can receive the csv as an array of arrays.
// d3.text("/visit-sequences.csv", function(text) {
  // var csv = d3.csv.parseRows(text);
  // var json = buildHierarchy(csv);
  // console.log(JSON.stringify(json));
  // createVisualization(json);
// });

var color = d3.scale.category20();
var legendD3 = {};

function createSunburstChart(jsonData) {
	createVisualization(jsonData);
}

// Main function to draw and set up the visualization, once we have the data.
function createVisualization(json) {

  for(var i = 0; i < legend.length; i++){
	legendD3[(legend[i].id)] = {'name': ((legend[i].name) ? legend[i].name : legend[i].id), 'fontColor': ((colors[(legend[i].id)]) ? colors[(legend[i].id)].font : '#ffffff'), 'backgroundColor': ((colors[(legend[i].id)]) ? colors[(legend[i].id)].background : color(legend[i].id.replace(/ .*/, ""))), 'position': ((legend[i].position) ? legend[i].position : 1000000)};
  }

  // Basic setup of page elements.
  initializeBreadcrumbTrail();
  drawLegend();

  // Bounding circle underneath the sunburst, to make it easier to detect
  // when the mouse leaves the parent g.
  vis.append("svg:circle")
      .attr("r", radius)
      .style("opacity", 0);

  // For efficiency, filter nodes to keep only those large enough to see.
  var nodes = partition.nodes(json)
      .filter(function(d) {
      return (d.dx > 0.005); // 0.005 radians = 0.29 degrees
      });

  var path = vis.data([json]).selectAll("path")
      .data(nodes)
      .enter().append("svg:path")
      .sort(function(a, b){var posA = ((legend[a.name]) ? legendD3[legend[a.name].id].position : 1000000); var posB = ((legend[b.name]) ? legendD3[legend[b.name].id].position : 1000000); return (posA - posB);})
      .attr("display", function(d) { return d.depth ? null : "none"; })
      .attr("d", arc)
      .attr("fill-rule", "evenodd")
      .style("fill", function(d) { return ((legend[d.name]) ? legendD3[legend[d.name].id].backgroundColor : undefined); })
      .style("opacity", 1)
      .on("mouseover", mouseover);

  // Add the mouseleave handler to the bounding circle.
  d3.select("#container").on("mouseleave", mouseleave);

  // Get total size of the tree = value of root node from partition.
  totalSize = data.value;
 };

// Fade all but the current sequence, and show it in the breadcrumb trail.
function mouseover(d) {

  var percentage = (100 * d.value / totalSize).toPrecision(3);
  var percentageString = percentage + "%";
  if (percentage < 0.1) {
    percentageString = "< 0.1%";
  }

  d3.select("#percentage")
      .text(percentageString);

  d3.select("#explanation")
      .style("visibility", "");

  var sequenceArray = getAncestors(d);
  updateBreadcrumbs(sequenceArray, percentageString);

  // Fade all the segments.
  d3.selectAll("path")
      .style("opacity", 0.3);

  // Then highlight only those that are an ancestor of the current segment.
  vis.selectAll("path")
      .filter(function(node) {
                return (sequenceArray.indexOf(node) >= 0);
              })
      .style("opacity", 1);
}

// Restore everything to full opacity when moving off the visualization.
function mouseleave(d) {

  // Hide the breadcrumb trail
  d3.select("#trail")
      .style("visibility", "hidden");

  // Deactivate all segments during transition.
  d3.selectAll("path").on("mouseover", null);

  // Transition each segment to full opacity and then reactivate it.
  d3.selectAll("path")
      .transition()
      .duration(1000)
      .style("opacity", 1)
      .each("end", function() {
              d3.select(this).on("mouseover", mouseover);
            });

  d3.select("#explanation")
      .transition()
      .duration(1000)
      .style("visibility", "hidden");
}

// Given a node in a partition layout, return an array of all of its ancestor
// nodes, highest first, but excluding the root.
function getAncestors(node) {
  var path = [];
  var current = node;
  while (current.parent) {
    path.unshift(current);
    current = current.parent;
  }
  return path;
}

function initializeBreadcrumbTrail() {
  // Add the svg area.
  var trail = d3.select("#sequence").append("svg:svg")
      .attr("width", width)
      .attr("height", 50)
      .attr("id", "trail");
  // Add the label at the end, for the percentage.
  trail.append("svg:text")
    .attr("id", "endlabel")
    .style("fill", "#000");
}

// Generate a string that describes the points of a breadcrumb polygon.
function breadcrumbPoints(d, i) {
  var points = [];
  points.push("0,0");
  points.push(b.w + ",0");
  points.push(b.w + b.t + "," + (b.h / 2));
  points.push(b.w + "," + b.h);
  points.push("0," + b.h);
  if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
    points.push(b.t + "," + (b.h / 2));
  }
  return points.join(" ");
}

// Update the breadcrumb trail to show the current sequence and percentage.
function updateBreadcrumbs(nodeArray, percentageString) {

  // Data join; key function combines name and depth (= position in sequence).
  var g = d3.select("#trail")
      .selectAll("g")
      .data(nodeArray, function(d) { return d.name * 1000 + d.depth; });

  // Add breadcrumb and label for entering nodes.
  var entering = g.enter().append("svg:g");
  entering.append("svg:polygon")
      .attr("points", breadcrumbPoints)
      .style("fill", function(d) { return ((legend[d.name]) ? legendD3[legend[d.name].id].backgroundColor : undefined); });

  entering.append("svg:text")
      .attr("x", (b.w + b.t) / 2)
      .attr("y", b.h / 2)
      .attr("dy", "0.35em")
      .attr("text-anchor", "middle")
      .style("fill", function(d) { return ((legend[d.name]) ? legendD3[legend[d.name].id].fontColor : undefined); })
      .text(function(d) { return ((legend[d.name]) ? ((legend[d.name].name) ? legend[d.name].name : legend[d.name].id) : 'Root'); });
      
  // Set position for entering and updating nodes.
  g.attr("transform", function(d, i) {
    return "translate(" + i * (b.w + b.s) + ", 0)";
  });

  // Remove exiting nodes.
  g.exit().remove();

  // Now move and update the percentage at the end.
  d3.select("#trail").select("#endlabel")
      .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
      .attr("y", b.h / 2)
      .attr("dy", "0.35em")
      .attr("text-anchor", "middle")
      .text(percentageString);

  // Make the breadcrumb trail visible, if it's hidden.
  d3.select("#trail")
      .style("visibility", "");

}

function drawLegend() {

  // Dimensions of legend item: width, height, spacing, radius of rounded rect.
  var li = {
    w: 150, h: 25, s: 1, r: 0
  };

  var l = d3.select("#legend").append("svg:svg")
      .attr("width", li.w)
      .attr("height", d3.keys(legendD3).length * (li.h + li.s));

  var g = l.selectAll("g")
      .data(d3.entries(legendD3).sort(function(a, b){var posA = ((a.value.position) ? a.value.position : 1000000); var posB = ((b.value.position) ? b.value.position : 1000000); return (posA - posB);}))
      .enter().append("svg:g")
      .attr("transform", function(d, i) {
              return "translate(0," + i * (li.h + li.s) + ")";
           });

  g.append("svg:rect")
      .attr("rx", li.r)
      .attr("ry", li.r)
      .attr("width", li.w)
      .attr("height", li.h)
      .style("fill", function(d) { return d.value.backgroundColor; });

  g.append("svg:text")
      .attr("x", li.w / 2)
      .attr("y", li.h / 2)
      .attr("dy", "0.35em")
      .attr("text-anchor", "middle")
      .style("fill", function(d) { return d.value.fontColor; })
      .text(function(d) { return d.value.name; });
}