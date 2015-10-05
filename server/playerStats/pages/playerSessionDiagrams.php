<?php
if(!in_array($_POST['chart'], array('sankey', 'sunburst', 'treemap'))){
	$_POST['chart'] = 'sunburst';
}

?>
<form action="?<?=generateDataTableQueryString($_GET)?>" method="post" class="border">
 <div class="col border">
  <div class="row">
   <label>&nbsp;</label>
   <input type="submit" name="apply" value="&lt;&lt;&lt; back to listing" />
  </div>
 </div>
</form>
<form action="?<?=generateDataTableQueryString($_GET)?>" method="post">
 <div class="col">
  <div class="row">
   <label>Diagram type</label>
   <select name="chart" onchange="$(this).closest('form').submit()">
    <!--<option value="sankey" <?=(($_POST['chart'] == 'sankey') ? 'selected="selected"' : '')?>>Sankey</option>//-->
	<option value="sunburst" <?=(($_POST['chart'] == 'sunburst') ? 'selected="selected"' : '')?>>Sunburst</option>
	<option value="treemap" <?=(($_POST['chart'] == 'treemap') ? 'selected="selected"' : '')?>>Treemap</option>
   </select>
   <input type="hidden" name="diagrams" value="true" />
  </div>
 </div>
</form>
<script src="/js/d3.v3.min.js"></script>
<?php
include(dirname(__FILE__).'/includes/'.$_POST['chart'].'Chart.php');
?>